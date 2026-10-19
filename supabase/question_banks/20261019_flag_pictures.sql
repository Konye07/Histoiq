-- Flag pictures that were not the flag named under them. Found on 2026-10-19 by looking at every flag in the bank; the right
-- file was then fetched from Wikimedia Commons, with the owner's permission, and its license read on its Commons page. The
-- pictures themselves are composeResources/files/images/flags/<same name>.png (960 px wide renderings of the SVGs), so
-- image_asset does not change. Applied once on 2026-10-19.
--
--   068 First Austrian Republic         Flag of Austria (1230–1934, 1945–2000).svg                       Public domain
--   079 Irish Free State                Flag of Ireland.svg                                              Public domain
--   093 Weimar Republic, war ensign     War Ensign of Germany (1921–1933).svg                            Public domain
--   104 People's Republic of China      Flag of the People's Republic of China.svg                       Public domain
--   112 Mandatory Palestine             Ensign of the Palestine Mandate (1927–1948).svg                  Public domain
--   143 Transvaal                       Flag of Transvaal.svg (Himasaram et al.)                         CC BY-SA 3.0, so it carries a credit line
--   149 Belgian Congo                   Flag of the Congo Free State.svg (the colony kept it)            Public domain
--   202 Sweden, union flag              Swedish civil ensign (1844–1905).svg                             Public domain
--   247 Democratic Republic of Georgia  Flag of Georgia (1918–1921, 1-2).svg                             Public domain
--   276 French Somaliland               Flag of France.svg (what its Wikipedia article uses)             Public domain
--   281 Portuguese Macau                Flag of the Government of Portuguese Macau (1976–1999).svg       Public domain
--   362 Congo-Léopoldville              Flag of the Republic of the Congo (Léopoldville) (1960–1963).svg Public domain
--   478 Commonwealth of the Philippines Flag of the Philippines (1936–1985, 1986–1998).svg               Public domain
--   492 People's Republic of Poland     Flag of Poland (1928–1980).svg                                   Public domain
--   495 Latvia (interwar)               Flag of Latvia.svg                                               Public domain
--   496 Estonia (interwar)              Flag of Estonia.svg                                              Public domain
--
-- 151 French Equatorial Africa was looked at too and is right: its picture is the very file English Wikipedia shows for it
-- (Flag of French Equatorial Africa.svg, taken from a flag book), so it stays.

-- A credit line for a picture whose license asks for one (CC BY, CC BY-SA): shown with the flag in the game.
alter table public.flag_color_match_items add column if not exists credit text;

update public.flag_color_match_items f set source = v.source
from (values
  ('flag_068', 'Wikimedia Commons: Flag of Austria (1230–1934, 1945–2000).svg (Public domain)'),
  ('flag_079', 'Wikimedia Commons: Flag of Ireland.svg (Public domain)'),
  ('flag_093', 'Wikimedia Commons: War Ensign of Germany (1921–1933).svg (Public domain)'),
  ('flag_104', 'Wikimedia Commons: Flag of the People''s Republic of China.svg (Public domain)'),
  ('flag_112', 'Wikimedia Commons: Ensign of the Palestine Mandate (1927–1948).svg (Public domain)'),
  ('flag_143', 'Wikimedia Commons: Flag of Transvaal.svg (CC BY-SA 3.0)'),
  ('flag_149', 'Wikimedia Commons: Flag of the Congo Free State.svg (Public domain)'),
  ('flag_202', 'Wikimedia Commons: Swedish civil ensign (1844–1905).svg (Public domain)'),
  ('flag_247', 'Wikimedia Commons: Flag of Georgia (1918–1921, 1-2).svg (Public domain)'),
  ('flag_276', 'Wikimedia Commons: Flag of France.svg (Public domain)'),
  ('flag_281', 'Wikimedia Commons: Flag of the Government of Portuguese Macau (1976–1999).svg (Public domain)'),
  ('flag_362', 'Wikimedia Commons: Flag of the Republic of the Congo (Léopoldville) (1960–1963).svg (Public domain)'),
  ('flag_478', 'Wikimedia Commons: Flag of the Philippines (1936–1985, 1986–1998).svg (Public domain)'),
  ('flag_492', 'Wikimedia Commons: Flag of Poland (1928–1980).svg (Public domain)'),
  ('flag_495', 'Wikimedia Commons: Flag of Latvia.svg (Public domain)'),
  ('flag_496', 'Wikimedia Commons: Flag of Estonia.svg (Public domain)')
) as v(id, source)
where f.id = v.id;

update public.flag_color_match_items
set credit = 'Himasaram et al. · CC BY-SA 3.0 · Wikimedia Commons'
where id = 'flag_143';

-- The Daily Battle had left these ten out only because their picture was wrong (fame 1); back to the rating their own
-- difficulty gives (1 -> 3, 2 -> 2, 3 -> 1), the same rule 20261019_fame_ratings.sql applied to every flag.
update public.flag_color_match_items
set fame = case difficulty when 1 then 3 when 2 then 2 else 1 end
where id in ('flag_079', 'flag_093', 'flag_104', 'flag_143', 'flag_149', 'flag_202', 'flag_478', 'flag_492', 'flag_495', 'flag_496');

-- The picture is now the 1960 flag, which became the diagonal-striped one in 1963: the label follows it.
update public.flag_color_match_items
set flag_name = replace(flag_name, '1960–1965', '1960–1963'),
    flag_name_hu = replace(flag_name_hu, '1960–1965', '1960–1963'),
    flag_name_es = replace(flag_name_es, '1960–1965', '1960–1963'),
    flag_name_fr = replace(flag_name_fr, '1960–1965', '1960–1963'),
    flag_name_de = replace(flag_name_de, '1960–1965', '1960–1963'),
    flag_name_it = replace(flag_name_it, '1960–1965', '1960–1963')
where id = 'flag_362';

-- Two flags that never existed: the Anglo-Egyptian Sudan flew the British and the Egyptian flag side by side and had none
-- of its own, and the United Arab States (the United Arab Republic plus Yemen, 1958-61) had none either; its picture was
-- the flag of the United Arab Emirates, and the United Arab Republic's real flag is already flag_121. Neither was in a
-- daily quiz for 2026-10-18 or 2026-10-19. Their pictures (flag_anglo_egyptian_sudan.png, flag_united_arab_states.png) are
-- removed from the app; both are in git history before this commit.
delete from public.flag_color_match_items where id in ('flag_264', 'flag_391');

-- Restore (the exact rows as they were):
-- insert into public.flag_color_match_items (id, type, flag_name, image_asset, source, difficulty, flag_name_hu, flag_name_es, flag_name_fr, flag_name_de, flag_name_it, fame) values ('flag_264', 'COLOR_MATCH', 'Anglo-Egyptian Sudan (1899–1956)', 'images/flags/flag_anglo_egyptian_sudan.png', 'Wikimedia Commons (Public domain)', 3, 'Angol–egyiptomi Szudán (1899–1956)', 'Sudán anglo-egipcio (1899–1956)', 'Soudan anglo-égyptien (1899–1956)', 'Anglo-Ägyptischer Sudan (1899–1956)', 'Sudan anglo-egiziano (1899–1956)', 1) on conflict (id) do nothing;
-- insert into public.flag_color_match_items (id, type, flag_name, image_asset, source, difficulty, flag_name_hu, flag_name_es, flag_name_fr, flag_name_de, flag_name_it, fame) values ('flag_391', 'COLOR_MATCH', 'United Arab States (Egypt-Syria-Yemen, 1958–1961)', 'images/flags/flag_united_arab_states.png', 'Wikimedia Commons (Public domain)', 3, 'Egyesült Arab Államok (Egyiptom–Szíria–Jemen, 1958–1961)', 'Estados Árabes Unidos (Egipto-Siria-Yemen, 1958–1961)', 'États arabes unis (Égypte-Syrie-Yémen, 1958–1961)', 'Vereinigte Arabische Staaten (Ägypten-Syrien-Jemen, 1958–1961)', 'Stati Arabi Uniti (Egitto-Siria-Yemen, 1958–1961)', 1) on conflict (id) do nothing;
