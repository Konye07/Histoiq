-- Hungarian text for the True or False statements, next to statement_es/fr/de/it. Additive: builds
-- without Hungarian ignore the column, and the app falls back to the English text where it is null.
alter table public.true_false_items add column if not exists statement_hu text;
