-- A short write-up (2-4 sentences: what happened, why it is worth knowing) that Annalist and Herald show under the
-- answer. Nullable, so a question without one simply shows nothing, and older app versions ignore the column.
alter table public.year_guess_items add column if not exists description text;
alter table public.casualties_multiplier_items add column if not exists description text;
