-- Era game modes for Annalist (year_guess), Herald (casualties_multiplier) and Chancellor
-- (blind_map_battle). An era mode is a game key of the form '<game>:<era>', so it gets its own rows in
-- daily_quizzes / quiz_progress / score_history / game_stats with no schema change there.
--
-- Every question in the three dated banks is flagged with its era, derived from its date, and an era mode
-- recalls its questions straight from that flag. Each question is in exactly one era, so the flag is a single
-- column (era_key) rather than five 1/0 columns that would always hold exactly one 1: same information, one
-- column, and it cannot contradict itself. It is a stored generated column, so it is filled in for every
-- existing row now and for every question added later, and follows the date if the date is ever corrected.
--
-- Additive and backward compatible: builds that predate the era modes keep calling get_or_create_daily_quiz
-- with plain game keys, which behave exactly as before. Safe to run more than once.

-- The map bank had no date, so it could not be flagged like the other two. Every battle except one also
-- exists in the Herald bank under the same name, so the year is copied from there.
alter table public.blind_map_battle_items add column if not exists year integer;

update public.blind_map_battle_items b
set year = c.year
from public.casualties_multiplier_items c
where b.year is null and lower(trim(c.battle)) = lower(trim(b.battle));

-- Only map battle without a Herald counterpart: the Russian advance on San Stefano (Yesilkoy), 1878.
update public.blind_map_battle_items
set year = 1878
where battle = 'Battle of San Stefano' and year is null;

alter table public.blind_map_battle_items alter column year set not null;

-- The one place the era boundaries live. Strict, so a missing year is no era rather than the last one.
create or replace function public.era_for_year(p_year integer)
 returns text
 language sql
 immutable
 strict
 set search_path to ''
as $$
  select case
    when p_year < 476 then 'ancient'
    when p_year < 1453 then 'medieval'
    when p_year <= 1750 then 'early_modern'
    when p_year < 1900 then 'modern'
    else 'contemporary'
  end
$$;

revoke execute on function public.era_for_year(integer) from public, anon, authenticated;

-- The flag. If the boundaries above ever change, re-flag the stored values by rewriting the date column
-- (update <bank> set <date column> = <date column>): a generated column is recomputed whenever a column it
-- depends on is written. Not named "era": territory_compare_items already has a free-text era label.
alter table public.year_guess_items
  add column if not exists era_key text generated always as (public.era_for_year(correct_year)) stored;
alter table public.casualties_multiplier_items
  add column if not exists era_key text generated always as (public.era_for_year(year)) stored;
alter table public.blind_map_battle_items
  add column if not exists era_key text generated always as (public.era_for_year(year)) stored;

CREATE OR REPLACE FUNCTION public.get_or_create_daily_quiz(p_game_key text, p_date date, p_count integer)
 RETURNS text[]
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
declare
  -- p_game_key is either a plain game key (the all-time daily quiz) or '<game>:<era>' (an era-filtered
  -- one: only questions whose era_key is that era). Each is its own row per day in daily_quizzes, so an
  -- era mode has its own shared question set, its own history and its own cooldown.
  v_game text := split_part(p_game_key, ':', 1);
  v_era text := nullif(split_part(p_game_key, ':', 2), '');
  v_table text := v_game || '_items';
  v_era_filter text := '';
  -- Per-game repeat cooldown for the all-time quiz. Territory Compare's catalog (130 countries) can
  -- never satisfy a 15-day/60-per-day non-repeat guarantee (would need ~900 distinct countries), hence
  -- its own short window paired with a correspondingly small daily draw (see TerritoryCompareScreen.kt).
  -- year_guess/casualties_multiplier/blind_map_battle are all battle/event-driven content
  -- (a specific battle, e.g. "Battle of Mohacs", is real, nameable content a player will
  -- remember) and draw only 5/day from 231-460 item catalogs, so 12 days (11 days blocked, back
  -- in the pool on the 12th) is a deliberate product choice here, not a capacity constraint like
  -- Territory Compare's — comfortably safe either way (12*5=60, far under any of their catalogs).
  -- Officer-of-Arms/Chronicler are unaffected, still 15.
  v_cooldown_days int;
  v_excluded text[];
  v_fresh text[];
  v_fresh_count integer;
  v_topup text[];
  v_ids text[];
begin
  select item_ids into v_ids from public.daily_quizzes
    where game_key = p_game_key and date_key = p_date;
  if v_ids is not null then
    return v_ids;
  end if;

  if position(':' in p_game_key) = 0 then
    v_cooldown_days := case v_game
      when 'territory_compare' then 4
      when 'year_guess' then 12
      when 'casualties_multiplier' then 12
      when 'blind_map_battle' then 12
      else 15
    end;
  else
    if v_era is null
       or p_game_key <> v_game || ':' || v_era
       or v_game not in ('year_guess', 'casualties_multiplier', 'blind_map_battle')
       or v_era not in ('ancient', 'medieval', 'early_modern', 'modern', 'contemporary') then
      raise exception 'unsupported quiz mode: %', p_game_key;
    end if;
    v_era_filter := format(' and era_key = %L', v_era);
    -- Era modes exclude a question for only 2 days: one used on day D is out for D+1 and D+2 and back on
    -- D+3 (the window below blocks cooldown-1 days, hence 3). Short on purpose — each era is a fraction
    -- of the catalog (the smallest, Herald's Early Modern, has 18 items against 5 drawn a day).
    v_cooldown_days := 3;
  end if;

  execute format(
    'select coalesce(array_agg(distinct id), %L) from public.daily_quizzes, unnest(item_ids) id
     where game_key = %L and date_key > %L::date - %s * interval ''1 day''',
    '{}', p_game_key, p_date, v_cooldown_days
  ) into v_excluded;

  -- Ids not used in the cooldown window, preferred first.
  execute format(
    'select array_agg(id) from (select id from %I where not (id = any(%L))%s order by random() limit %L) t',
    v_table, v_excluded, v_era_filter, p_count
  ) into v_fresh;
  v_fresh := coalesce(v_fresh, '{}');
  v_fresh_count := coalesce(array_length(v_fresh, 1), 0);

  -- Too few (including zero) cooldown-eligible ids to fill the pool — top up with the
  -- least-bad remaining option (previously-used ids) instead of silently shipping an
  -- undersized/degenerate pool. Excluding v_fresh here (not just v_excluded) keeps the two
  -- parts disjoint, so the combined pool is always p_count *distinct* ids (or the whole
  -- table, if smaller) rather than ever repeating an id within the same day's pool. The era
  -- filter still applies, so a top-up never pulls in another era's question.
  if v_fresh_count < p_count then
    execute format(
      'select array_agg(id) from (select id from %I where not (id = any(%L))%s order by random() limit %L) t',
      v_table, v_fresh, v_era_filter, p_count - v_fresh_count
    ) into v_topup;
    v_ids := v_fresh || coalesce(v_topup, '{}');
  else
    v_ids := v_fresh;
  end if;

  insert into public.daily_quizzes(game_key, date_key, item_ids)
  values (p_game_key, p_date, v_ids)
  on conflict (game_key, date_key) do nothing;

  select item_ids into v_ids from public.daily_quizzes
    where game_key = p_game_key and date_key = p_date;
  return v_ids;
end;
$function$;
