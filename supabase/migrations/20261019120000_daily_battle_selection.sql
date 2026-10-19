-- The Daily Battle, and a fairer way to pick every day's questions.
--
-- 1. Daily Battle: one more family of game keys, '<game>:battle' (six of them, one per game). A battle leg is the
--    day's one question of its game (two for Cosmographer, whose question is a pair of countries), so it gets its own
--    rows in daily_quizzes / quiz_progress, but it draws only well-known questions (the new `fame` rating on every
--    bank) and never shares a question with any other round of the same day. The combined battle score (six
--    questions, 0-6000) is stored by the client under the key 'daily_battle' in score_history, so get_final_scores /
--    game_stats already work for it.
--
-- 2. Less repetition. The old picker drew `order by random()` after a fixed cooldown per game, which let the same
--    event come back in a different game a day later (Annalist, Herald and Chancellor all keep their own copy of
--    most battles), put the same event in two games on the same day, and cycled an era mode's small pool every
--    3-7 days. The new picker works on an *event identity* (the normalized name + year shared by the three event
--    banks, with event_aliases merging the few events one bank names differently, e.g. "Battle of Tannenberg" =
--    "Battle of Grunwald") and chooses by weighted random sampling:
--      * the longer an identity has been out of its class of rounds (the plain all-time rounds, one era's rounds or
--        the battle's), the likelier it is;
--      * an identity already used today under any other key is practically excluded;
--      * an identity another class used in the last 3 days is much less likely;
--      * a battle leg prefers famous questions (fame 3 weighs x3, fame 2 x1) and takes a niche one (fame 1) only when
--        no famous or known one is left, not even a recently used one;
--      * a hard minimum gap per game keeps its old guarantee, but gives way before a round would come up short.
--    Nothing here is a hard failure: when a pool is too small the least recently used questions fill the round.
--    Validated on a scratch Postgres against a copy of the real banks and history, 60 simulated days, every key.

-- How well known a question is to a Western European or American player: 3 famous, 2 known, 1 niche.
-- Only the Daily Battle looks at it. New rows default to 2, so they are battle-eligible until rated.
alter table public.year_guess_items add column if not exists fame smallint not null default 2;
alter table public.casualties_multiplier_items add column if not exists fame smallint not null default 2;
alter table public.blind_map_battle_items add column if not exists fame smallint not null default 2;
alter table public.flag_color_match_items add column if not exists fame smallint not null default 2;
alter table public.territory_compare_items add column if not exists fame smallint not null default 2;
alter table public.true_false_items add column if not exists fame smallint not null default 2;

-- Chancellor now shows the same "why it matters" write-up as the other two dated games after an answer.
alter table public.blind_map_battle_items
  add column if not exists description text,
  add column if not exists description_hu text,
  add column if not exists description_es text,
  add column if not exists description_fr text,
  add column if not exists description_de text,
  add column if not exists description_it text;

-- One event, two names: both normalized keys resolve to one identity. Flat on purpose (a canon is never an alias).
create table if not exists public.event_aliases (
  alias_key text primary key,
  canon_key text not null check (alias_key <> canon_key)
);
alter table public.event_aliases enable row level security;
revoke all on table public.event_aliases from anon, authenticated;

insert into public.event_aliases (alias_key, canon_key)
select regexp_replace(lower(v.alias_name), '[^a-z0-9]', '', 'g') || '_' || v.yr,
       regexp_replace(lower(v.canon_name), '[^a-z0-9]', '', 'g') || '_' || v.yr
from (values
  (-1274, 'Battle of Kadesh (Egyptian-Levantine theatre)', 'Battle of Kadesh'),
  (-1046, 'Zhou conquest of Shang', 'Battle of Muye'),
  (-539, 'Cyrus conquers Babylon', 'Battle of Opis'),
  (-499, 'Persian Wars begin', 'Persian Wars'),
  (-334, 'Alexander the Great''s conquests', 'Alexander conquers Persia'),
  (-202, 'Battle of Gaixia', 'Han dynasty founded'),
  (312, 'Constantine''s conversion', 'Battle of Milvian Bridge'),
  (750, 'Battle of Zab', 'Abbasid Revolution'),
  (1066, 'Norman Conquest of England', 'Battle of Hastings'),
  (1098, 'Siege of Antioch', 'Battle of Antioch'),
  (1099, 'Siege of Jerusalem', 'Capture of Jerusalem by Crusaders'),
  (1258, 'Mongol sack of Baghdad', 'Siege of Baghdad'),
  (1410, 'Battle of Tannenberg', 'Battle of Grunwald'),
  (1453, 'Ottoman conquest of Constantinople', 'Fall of Constantinople'),
  (1492, 'European sighting of the Caribbean', 'Columbus reaches the Americas'),
  (1521, 'Siege of Tenochtitlan', 'Fall of Tenochtitlan'),
  (1532, 'Battle of Cajamarca', 'Spanish conquest of Peru'),
  (1789, 'Storming of the Bastille', 'French Revolution begins'),
  (1859, 'Italian unification', 'Battle of Solferino'),
  (1898, 'Battle of Manila Bay', 'Spanish-American War'),
  (1917, 'Bolshevik Revolution', 'Russian Revolution'),
  (1937, 'Nanjing Massacre', 'Battle of Nanjing'),
  (1941, 'Pearl Harbor attack', 'Attack on Pearl Harbor'),
  (1942, 'Naval Battle of Guadalcanal', 'Battle of Guadalcanal'),
  (1945, 'Fall of Berlin', 'Battle of Berlin'),
  (1947, 'Partition of India', 'Indian independence'),
  (1948, 'First Arab-Israeli War', 'Creation of Israel'),
  (1950, 'People''s Republic of China enters Korean War', 'Korean War begins'),
  (1971, 'Indo-Pakistani War', 'Bangladesh Liberation War'),
  (1975, 'Fall of Saigon / European détente era', 'Fall of Saigon')
) as v(yr, alias_name, canon_name)
on conflict (alias_key) do update set canon_key = excluded.canon_key;

-- Every question of every bank in one shape: its game, its identity (events: normalized name + year, merged through
-- event_aliases; everything else: its id), its fame and its era. Internal: only the picker below calls it.
create or replace function public._quiz_catalog()
 returns table(game text, id text, ik text, fame smallint, era_key text)
 language sql
 stable
 set search_path to 'public'
as $function$
  with ev as (
    select 'year_guess'::text as g, y.id, y.event as name, y.correct_year as yr, y.fame, y.era_key from public.year_guess_items y
    union all
    select 'casualties_multiplier', c.id, c.battle, c.year, c.fame, c.era_key from public.casualties_multiplier_items c
    union all
    select 'blind_map_battle', b.id, b.battle, b.year, b.fame, b.era_key from public.blind_map_battle_items b
  ), evk as (
    select g, ev.id, regexp_replace(lower(name), '[^a-z0-9]', '', 'g') || '_' || yr as rk, ev.fame, ev.era_key from ev
  )
  select evk.g, evk.id, coalesce(a.canon_key, evk.rk), evk.fame, evk.era_key
  from evk left join public.event_aliases a on a.alias_key = evk.rk
  union all select 'flag_color_match', f.id, f.id, f.fame, null::text from public.flag_color_match_items f
  union all select 'territory_compare', t.id, t.id, t.fame, null::text from public.territory_compare_items t
  union all select 'true_false', s.id, s.id, s.fame, null::text from public.true_false_items s
$function$;

revoke execute on function public._quiz_catalog() from public, anon, authenticated;

CREATE OR REPLACE FUNCTION public.get_or_create_daily_quiz(p_game_key text, p_date date, p_count integer)
 RETURNS text[]
 LANGUAGE plpgsql
 SECURITY DEFINER
 SET search_path TO 'public'
AS $function$
declare
  -- p_game_key is a plain game key (the all-time daily quiz), '<game>:<era>' (only that era's questions; Annalist,
  -- Herald and Chancellor) or '<game>:battle' (one leg of the Daily Battle). Each is its own row per day in
  -- daily_quizzes, so each has its own shared question set and its own history.
  v_game text := split_part(p_game_key, ':', 1);
  v_suffix text := nullif(split_part(p_game_key, ':', 2), '');
  v_battle boolean := coalesce(v_suffix = 'battle', false);
  v_era text := case when v_battle then null else v_suffix end;
  -- The class of rounds a key competes with for questions: the plain all-time rounds, one era's rounds, or the
  -- battle's. A question's recency is judged inside its own class, so an era mode's small pool (which it cycles every
  -- few days) cannot push its questions out of the all-time rounds, and the other way round.
  v_class text := coalesce(v_suffix, '');
  v_count integer := least(greatest(coalesce(p_count, 1), 1), 60);
  -- Days after it was shown that a question stays all but out of its own class (see the weights below). Territory
  -- Compare's catalog is small and a round needs 11 of it; the era and battle pools are small too.
  v_hard_gap integer;
  v_ids text[];
begin
  select item_ids into v_ids from public.daily_quizzes
    where game_key = p_game_key and date_key = p_date;
  if v_ids is not null then
    return v_ids;
  end if;

  if v_game not in ('year_guess', 'casualties_multiplier', 'flag_color_match', 'territory_compare', 'true_false', 'blind_map_battle') then
    raise exception 'unsupported quiz mode: %', p_game_key;
  end if;
  if position(':' in p_game_key) > 0 then
    if v_suffix is null or p_game_key <> v_game || ':' || v_suffix then
      raise exception 'unsupported quiz mode: %', p_game_key;
    end if;
    if not v_battle and (v_game not in ('year_guess', 'casualties_multiplier', 'blind_map_battle')
       or v_era not in ('ancient', 'medieval', 'early_modern', 'modern', 'contemporary')) then
      raise exception 'unsupported quiz mode: %', p_game_key;
    end if;
  end if;

  v_hard_gap := case
    when v_battle then case v_game when 'territory_compare' then 3 when 'true_false' then 14 when 'flag_color_match' then 6 else 5 end
    when v_era is not null then 2
    else case v_game when 'territory_compare' then 3 when 'true_false' then 14 when 'flag_color_match' then 14 else 11 end
  end;

  with
  allcat as materialized (
    select c.game, c.id, c.ik, c.fame, c.era_key from public._quiz_catalog() c
  ),
  cand as (
    select a.id, a.ik, a.fame from allcat a
    where a.game = v_game and (v_era is null or a.era_key = v_era)
  ),
  hist as (
    select q.game_key, q.date_key, a.ik, coalesce(nullif(split_part(q.game_key, ':', 2), ''), '') as cls
    from public.daily_quizzes q
    cross join lateral unnest(q.item_ids) as u(id)
    join allcat a on a.id = u.id
    where q.date_key between p_date - 150 and p_date
  ),
  fam as (
    select h.ik, min(p_date - h.date_key) as gap from hist h
    where h.date_key < p_date and h.cls = v_class
    group by h.ik
  ),
  oth as (
    select h.ik, min(p_date - h.date_key) as gap from hist h
    where h.date_key < p_date and h.cls <> v_class
    group by h.ik
  ),
  today as (
    select distinct h.ik from hist h where h.date_key = p_date and h.game_key <> p_game_key
  ),
  -- The battles Herald and Chancellor can ask about: few enough that the Annalist leg should leave them to those two
  -- legs unless it has nothing else (there are far more well-known events that are not battles).
  battles as (
    select distinct a.ik from allcat a where a.game in ('casualties_multiplier', 'blind_map_battle')
  ),
  scored as (
    select distinct on (c.ik) c.id,
      power(least(coalesce(f.gap, 150), 150) + 1, 2)
        * case when f.gap <= v_hard_gap then 1e-4 else 1 end
        * case when o.gap <= 3 then 0.3 else 1 end
        -- The three exclusions below are ordered by how much they matter: a question that already runs today under
        -- another key is the last resort of all, a niche one (battle only) the next to last, a recent one comes first.
        * case when t.ik is not null then 1e-20 else 1 end
        * case when v_battle then (case c.fame when 3 then 3.0 when 2 then 1.0 else 1e-12 end) else 1 end
        * case when v_battle and v_game = 'year_guess' and b.ik is not null then 0.25 else 1 end
        as w
    from cand c
    left join fam f on f.ik = c.ik
    left join oth o on o.ik = c.ik
    left join today t on t.ik = c.ik
    left join battles b on b.ik = c.ik
    order by c.ik, random()
  ),
  ranked as (
    select s.id, ln(1 - random()) / s.w as k from scored s
  )
  select coalesce(array_agg(r.id order by r.k desc), '{}')
  into v_ids
  from (select id, k from ranked order by k desc limit v_count) r;

  insert into public.daily_quizzes(game_key, date_key, item_ids)
  values (p_game_key, p_date, v_ids)
  on conflict (game_key, date_key) do nothing;

  select item_ids into v_ids from public.daily_quizzes
    where game_key = p_game_key and date_key = p_date;
  return v_ids;
end;
$function$;
