-- Spanish, French, German and Italian text for flag names, countries, eras and descriptions.
alter table public.territory_compare_items
  add column if not exists country_es text, add column if not exists country_fr text, add column if not exists country_de text, add column if not exists country_it text,
  add column if not exists era_es text, add column if not exists era_fr text, add column if not exists era_de text, add column if not exists era_it text;
alter table public.flag_color_match_items
  add column if not exists flag_name_es text, add column if not exists flag_name_fr text, add column if not exists flag_name_de text, add column if not exists flag_name_it text;
alter table public.year_guess_items
  add column if not exists description_es text, add column if not exists description_fr text, add column if not exists description_de text, add column if not exists description_it text;
alter table public.casualties_multiplier_items
  add column if not exists description_es text, add column if not exists description_fr text, add column if not exists description_de text, add column if not exists description_it text;
