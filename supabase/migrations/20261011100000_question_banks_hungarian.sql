-- Hungarian text for the question banks, next to true_false_items.statement_hu. Additive: builds without
-- Hungarian ignore the columns, and the app falls back to the English text where one is null.
alter table public.year_guess_items
  add column if not exists event_hu text,
  add column if not exists hint_hu text,
  add column if not exists description_hu text;

alter table public.casualties_multiplier_items
  add column if not exists battle_hu text,
  add column if not exists hint_hu text,
  add column if not exists description_hu text;

alter table public.blind_map_battle_items
  add column if not exists battle_hu text;

alter table public.flag_color_match_items
  add column if not exists flag_name_hu text;

alter table public.territory_compare_items
  add column if not exists country_hu text,
  add column if not exists era_hu text;
