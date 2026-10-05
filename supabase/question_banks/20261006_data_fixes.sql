-- Corrections found while writing the question descriptions. Idempotent; already applied to production.

-- "Battle of Bi" (Jin against Chu) was stored as 632 BCE in all three banks; it was fought in 597 BCE.
update public.year_guess_items set correct_year = -597 where id = 'event_345' and correct_year <> -597;
update public.casualties_multiplier_items set year = -597 where id = 'battle_118' and year <> -597;
update public.blind_map_battle_items set year = -597 where id = 'battlemap_117' and year <> -597;

-- "Siege of Jerusalem" (1099) was hinted "Mithridatic Wars"; it belongs to the First Crusade.
update public.year_guess_items set hint = 'First Crusade' where id = 'event_319' and hint is distinct from 'First Crusade';
