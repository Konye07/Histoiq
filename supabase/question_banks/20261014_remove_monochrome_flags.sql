-- Officer-of-Arms recolors a flag's dominant non-neutral color. Courland and Sicily are drawn in pure black and
-- white (no pixel has any color), so there is nothing to recolor in them and the question could not be played.
-- Removed from production on 2026-10-14. Neither was in any daily quiz after 2026-09-29, so no round in progress
-- was affected. New flags need at least one real color for the same reason.
delete from public.flag_color_match_items where id in ('flag_178', 'flag_493');

-- Restore (the exact rows as they were):
-- insert into public.flag_color_match_items (id, type, flag_name, image_asset, source, difficulty, flag_name_hu, flag_name_es, flag_name_fr, flag_name_de, flag_name_it) values ('flag_178', 'COLOR_MATCH', 'Kingdom of Sicily (1130–1816)', 'images/flags/flag_kingdom_of_sicily.png', 'Wikimedia Commons (CC BY-SA 4.0)', '2', 'Szicíliai Királyság (1130–1816)', 'Reino de Sicilia (1130–1816)', 'Royaume de Sicile (1130–1816)', 'Königreich Sizilien (1130–1816)', 'Regno di Sicilia (1130–1816)') on conflict (id) do nothing;
-- insert into public.flag_color_match_items (id, type, flag_name, image_asset, source, difficulty, flag_name_hu, flag_name_es, flag_name_fr, flag_name_de, flag_name_it) values ('flag_493', 'COLOR_MATCH', 'Baltic Duchy of Courland (1918)', 'images/flags/flag_baltic_duchy_of_courland.png', 'Wikimedia Commons (Public domain)', '3', 'Kurlandi Balti Hercegség (1918)', 'Ducado Báltico de Curlandia (1918)', 'Duché balte de Courlande (1918)', 'Baltisches Herzogtum Kurland (1918)', 'Ducato baltico di Curlandia (1918)') on conflict (id) do nothing;
-- (the two image files are in git history before the commit that removed them)
