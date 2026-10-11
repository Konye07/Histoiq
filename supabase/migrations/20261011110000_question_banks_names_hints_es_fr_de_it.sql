-- Spanish, French, German and Italian text for the names and hints of the three dated banks.
-- Descriptions, flag names and country names stay English (Hungarian has them, see 20261011100000).
alter table public.year_guess_items
  add column if not exists event_es text, add column if not exists event_fr text, add column if not exists event_de text, add column if not exists event_it text,
  add column if not exists hint_es text, add column if not exists hint_fr text, add column if not exists hint_de text, add column if not exists hint_it text;
alter table public.casualties_multiplier_items
  add column if not exists battle_es text, add column if not exists battle_fr text, add column if not exists battle_de text, add column if not exists battle_it text,
  add column if not exists hint_es text, add column if not exists hint_fr text, add column if not exists hint_de text, add column if not exists hint_it text;
alter table public.blind_map_battle_items
  add column if not exists battle_es text, add column if not exists battle_fr text, add column if not exists battle_de text, add column if not exists battle_it text;
