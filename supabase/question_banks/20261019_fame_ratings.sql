-- How well known each question is to a Western European or American player (3 famous, 2 known, 1 niche). Only the
-- Daily Battle reads it: a battle leg draws 3 x as often from the famous, as often from the known, and from the niche
-- only when nothing else is left (see get_or_create_daily_quiz). Every other round ignores it.
--
-- Events (Annalist, Herald and Chancellor): rated once per event, wherever the event appears, by the same normalized
-- name + year key the picker uses for event identity. Everything not listed is 2. The Annalist events that are not a
-- battle (treaties, revolutions, founding dates) are rated like the rest; recent politics (ISIS, the Abraham Accords,
-- the Iran deal, the Syrian and Gaza wars) is niche on purpose: the battle is meant to be history.
-- Flags and countries: from the question's own difficulty (1 -> 3, 2 -> 2, 3 -> 1), with the exceptions below.
-- True/false statements: from difficulty (1 -> 3, 2 -> 2, 3 and 4 -> 1).
--
-- Applied once on 2026-10-19; the block aborts (and changes nothing) if a listed event matches no question.
do $fame$
declare
  v_missing text;
  v_famous text := $famous$
-776|First Olympic Games (traditional date)
-490|Battle of Marathon
-480|Battle of Salamis
-480|Battle of Thermopylae
-399|Trial and death of Socrates
-334|Alexander conquers Persia
-334|Alexander the Great's conquests
-216|Battle of Cannae (North African Carthaginian army)
-73|Spartacus revolt
-49|Caesar crosses the Rubicon
-44|Julius Caesar assassinated
-31|Battle of Actium
-27|Augustus becomes first Roman emperor
476|Fall of Western Roman Empire
732|Battle of Tours
1066|Battle of Hastings
1066|Norman Conquest of England
1096|First Crusade
1099|Capture of Jerusalem by Crusaders
1099|Siege of Jerusalem
1215|Magna Carta
1337|Hundred Years' War
1347|Black Death reaches Europe
1415|Battle of Agincourt
1450|Gutenberg printing revolution
1453|Fall of Constantinople
1453|Ottoman conquest of Constantinople
1492|Columbus reaches the Americas
1492|European sighting of the Caribbean
1517|Protestant Reformation begins
1588|Spanish Armada
1773|Boston Tea Party
1776|Declaration of Independence
1781|Battle of Yorktown
1789|French Revolution begins
1789|Storming of the Bastille
1803|Louisiana Purchase
1804|Napoleon becomes emperor
1805|Battle of Austerlitz
1815|Battle of Waterloo
1848|California Gold Rush
1861|American Civil War
1863|Battle of Gettysburg
1863|Emancipation Proclamation
1914|World War I begins
1915|Battle of Gallipoli
1916|Battle of the Somme
1916|Battle of Verdun
1917|Bolshevik Revolution
1917|Russian Revolution
1919|Treaty of Versailles
1929|Great Depression begins
1933|Nazi seizure of power
1938|Munich Agreement
1939|World War II begins in Europe
1940|Battle of Britain
1940|Fall of France
1941|Attack on Pearl Harbor
1941|Pearl Harbor attack
1941|Operation Barbarossa
1942|Battle of Midway
1942|Battle of Stalingrad
1944|D-Day
1945|Atomic bombings of Hiroshima and Nagasaki
1945|Battle of Berlin
1945|Fall of Berlin
1945|Battle of Iwo Jima
1955|Vietnam War
1961|Berlin Wall built
1962|Cuban Missile Crisis
1969|Moon landing
1986|Chernobyl Disaster
1989|Fall of Berlin Wall
1991|Dissolution of Soviet Union
2001|September 11 attacks
2003|U.S.-led invasion of Iraq
2016|Brexit referendum
$famous$;
  v_niche text := $niche$
-1046|Battle of Muye
-1046|Zhou conquest of Shang
-853|Battle of Qarqar
-684|Battle of Changshao
-597|Battle of Bi
-575|Battle of Yanling
-539|Battle of Opis
-525|Battle of Pelusium
-424|Battle of Delium
-422|Battle of Amphipolis
-418|Battle of Mantinea (418 BC)
-394|Battle of Nemea (394 BC)
-371|Battle of Leuctra
-332|Siege of Gaza
-330|Battle of Persian Gate
-326|Battle of Hydaspes
-312|Seleucid Empire established
-305|Ptolemaic rule in Egypt
-295|Battle of Sentinum
-280|Battle of Heraclea
-279|Battle of Asculum
-260|Battle of Changping
-256|Battle of Carthage (First Punic War)
-225|Battle of Telamon
-217|Battle of Raphia
-207|Battle of Julu
-207|Battle of the Metaurus
-202|Battle of Gaixia
-197|Battle of Cynoscephalae
-190|Battle of Magnesia
-168|Battle of Pydna
-146|Roman conquest of Greece
-105|Battle of Arausio
-87|Siege of Athens and Piraeus
-63|Siege of Jerusalem
-46|Battle of Thapsus
-46|Battle of Utica
-45|Battle of Munda
-30|Roman annexation of Egypt
83|Battle of Mons Graupius
200|Battle of Guandu
217|Battle of Nisibis
224|Sasanian Empire founded
234|Battle of Wuzhang Plains
260|Battle of Edessa
280|Jin reunification
351|Battle of Mursa Major
357|Battle of Strasbourg
383|Battle of Fei River
507|Battle of Zhongli
530|Battle of Dara
530|Battle of Satala
533|Battle of Ad Decimum
540|Battle of Sura
589|Sui reunifies China
612|Battle of Salsu
634|Battle of the Bridge
636|Battle of Qadisiyyah
638|Conquest of Jerusalem
639|Arab conquest of Egypt
684|Battle of Marj Rahit
691|Battle of Maskin
698|Battle of Carthage (698)
750|Abbasid Revolution
750|Battle of Zab
751|Battle of Talas
755|An Lushan Rebellion
756|Battle of Yongqiu
757|Battle of Suiyang
757|Battle of Xiangji Temple
811|Battle of Pliska
871|Battle of Ashdown
899|Battle of Brenta
960|Song dynasty founded
969|Founding of Cairo
1014|Battle of Kleidion
1086|Battle of Sagrajas
1096|Battle of Civetot
1099|Battle of Ascalon
1192|Battle of Jaffa
1195|Battle of Alarcos
1213|Battle of Muret
1223|Battle of Kalka River
1241|Battle of Mohi
1260|Battle of Ain Jalut
1260|Siege of Aleppo
1260|Siege of Damascus
1271|Mongol conquest of Song
1277|Battle of Elbistan
1291|Battle of Acre
1299|Battle of Wadi al-Khazandar
1428|Rise of the Aztec Triple Alliance
1444|Battle of Varna
1456|Siege of Belgrade (1456)
1465|Battle of Vajkal
1475|Battle of Vaslui
1479|Battle of Breadfield
1480|Great Stand on the Ugra River
1483|Battle of Lucena
1487|Siege of Málaga
1495|Battle of Fornovo
1501|Safavid Empire founded
1503|Battle of Garigliano
1512|Battle of Ravenna
1513|Battle of Novara
1514|Battle of Chaldiran
1514|Battle of Orsha
1517|Ottoman conquest of Egypt
1522|Battle of Bicocca
1525|Battle of Frankenhausen
1526|Battle of Mohacs
1531|Battle of Obertyn
1532|Battle of Quipaipán
1538|Battle of Las Salinas
1542|Battle of Chupas
1544|Battle of Ceresole
1545|Council of Trent
1547|Battle of Mühlberg
1547|Battle of Pinkie Cleugh
1552|Siege of Eger
1555|Peace of Augsburg
1562|Battle of Dreux
1566|Siege of Szigetvár
1568|Battle of Heiligerlee
1569|Battle of Moncontour
1578|Battle of Ksar el Kebir
1587|Battle of Coutras
1590|Battle of Ivry
1591|Moroccan conquest of Songhai
1593|Battle of Sisak
1596|Battle of Mezőkeresztes
1600|Battle of Nieuwpoort
1605|Battle of Kircholm
1610|Battle of Klushino
1622|Battle of Wimpfen
1626|Battle of Lutter
1631|Sack of Magdeburg
1632|Battle of Rain
1632|Battle of the Alte Veste
1634|Battle of Nördlingen
1636|Battle of Wittstock
1644|Battle of Shanhai Pass
1645|Battle of Jankau
1652|Cape Colony established
1664|Battle of Saint Gotthard
1680|Pueblo Revolt
1686|Siege of Buda (1686)
1687|Second Battle of Mohács
1691|Battle of Slankamen
1697|Battle of Zenta
1700|Battle of Narva
1711|Battle of Pruth
1716|Battle of Petrovaradin
1716|Siege of Temesvár
1739|Battle of Grocka
1757|Battle of Prague
1763|Treaty of Paris ends Seven Years' War in North America
1775|Battle of the Great Bridge
1780|Battle of Camden
1780|Battle of Charleston
1781|Battle of Guilford Court House
1794|French abolition of slavery in colonies
1800|Battle of Heliopolis
1812|Battle of Queenston Heights
1813|Battle of Dresden
1814|Battle of Fort Erie
1825|Bolivian independence
1827|Battle of Navarino
1832|Battle of Konya
1839|Battle of Nezib
1842|Treaty of Nanking
1846|Battle of Monterrey
1846|Battle of Palo Alto
1846|Battle of Resaca de la Palma
1846|Battle of San Pasqual
1847|Battle of Sacramento
1850|Compromise of 1850
1854|Kansas-Nebraska Act
1855|Battle of Kars
1857|Mexican Reform War
1858|Battle of Monterrey (Reform War)
1862|Battle of Stones River
1864|Battle of Mansfield
1865|U.S. Reconstruction begins
1871|Battle of Le Mans
1876|Ottoman constitutional era
1877|Battle of Plevna
1878|Battle of San Stefano
1887|Battle of Dogali
1894|Sino-Japanese War
1896|Battle of Adwa
1898|Hundred Days' Reform
1899|Battle of Colenso
1900|Battle of Spion Kop
1904|Battle of Port Arthur
1905|Iranian Constitutional Revolution
1908|Young Turk Revolution
1911|Battle of Ciudad Juárez
1911|Xinhai Revolution
1912|First Balkan War
1912|Republic of China founded
1914|Battle of Galicia
1914|Battle of Kilimanjaro
1914|Battle of Łódź
1914|Battle of Tanga
1914|Battle of the Frontiers
1914|Battle of Veracruz
1916|Brusilov Offensive
1917|Battle of Cambrai
1917|Battle of Gaza
1917|Battle of Mahenge
1917|Battle of Mahiwa
1918|Battle of Aleppo
1918|Battle of Damascus
1918|Battle of Namacurra
1918|Hundred Days Offensive
1918|Spring Offensive
1919|May Fourth Movement
1920|Battle of Warsaw
1920|Treaty of Trianon
1921|Chinese Communist Party founded
1923|Treaty of Lausanne
1932|Creation of Saudi Arabia
1934|Long March
1937|Battle of Nanjing
1937|Battle of Shanghai
1937|Nanjing Massacre
1937|Second Sino-Japanese War
1938|Battle of Wuhan
1939|Battle of Khalkhin Gol
1941|Battle of Hong Kong
1941|Battle of Kiev
1941|Battle of Malaya
1941|Battle of Smolensk
1941|Battle of South Shanxi
1942|Battle of Kharkov
1944|Battle of Guam
1944|Battle of the Philippine Sea
1944|Operation Bagration
1945|Battle of Manchuria
1945|Battle of Manila
1945|Battle of Saigon
1945|Vietnamese independence declaration
1946|First Indochina War
1950|People's Republic of China enters Korean War
1951|European Coal and Steel Community
1957|Ghana independence
1958|Great Leap Forward
1969|Battle of Hamburger Hill
1971|Bangladesh Liberation War
1971|Indo-Pakistani War
1973|Chile coup
1975|Lebanese Civil War
1978|Deng Xiaoping reforms
1986|Doi Moi reforms
1992|Maastricht Treaty
1993|Oslo Accords
1999|Macau handover
2001|China joins WTO
2004|EU enlargement eastward
2008|Global financial crisis in Europe
2011|Syrian Civil War begins
2013|Rise of ISIS
2015|Iran nuclear deal
2020|Abraham Accords
2023|Israel-Gaza war begins
$niche$;
begin
  create temp table _event_fame on commit drop as
    select split_part(l, '|', 1)::int as yr, substr(l, strpos(l, '|') + 1) as name, 3::smallint as fame
    from unnest(string_to_array(v_famous, E'\n')) l where l <> ''
    union all
    select split_part(l, '|', 1)::int, substr(l, strpos(l, '|') + 1), 1::smallint
    from unnest(string_to_array(v_niche, E'\n')) l where l <> '';

  select string_agg(f.yr || '|' || f.name, E'\n') into v_missing
  from _event_fame f
  where not exists (
    select 1 from (
      select regexp_replace(lower(event), '[^a-z0-9]', '', 'g') || '_' || correct_year as rk from public.year_guess_items
      union all select regexp_replace(lower(battle), '[^a-z0-9]', '', 'g') || '_' || year from public.casualties_multiplier_items
      union all select regexp_replace(lower(battle), '[^a-z0-9]', '', 'g') || '_' || year from public.blind_map_battle_items
    ) k
    where k.rk = regexp_replace(lower(f.name), '[^a-z0-9]', '', 'g') || '_' || f.yr
  );
  if v_missing is not null then
    raise exception E'no such event in any bank:\n%', v_missing;
  end if;
  if exists (select 1 from _event_fame group by yr, name having count(*) > 1) then
    raise exception 'an event is rated twice';
  end if;

  update public.year_guess_items set fame = 2;
  update public.casualties_multiplier_items set fame = 2;
  update public.blind_map_battle_items set fame = 2;

  update public.year_guess_items t set fame = f.fame from _event_fame f
    where regexp_replace(lower(t.event), '[^a-z0-9]', '', 'g') || '_' || t.correct_year = regexp_replace(lower(f.name), '[^a-z0-9]', '', 'g') || '_' || f.yr;
  update public.casualties_multiplier_items t set fame = f.fame from _event_fame f
    where regexp_replace(lower(t.battle), '[^a-z0-9]', '', 'g') || '_' || t.year = regexp_replace(lower(f.name), '[^a-z0-9]', '', 'g') || '_' || f.yr;
  update public.blind_map_battle_items t set fame = f.fame from _event_fame f
    where regexp_replace(lower(t.battle), '[^a-z0-9]', '', 'g') || '_' || t.year = regexp_replace(lower(f.name), '[^a-z0-9]', '', 'g') || '_' || f.yr;

  update public.flag_color_match_items set fame = case difficulty when 1 then 3 when 2 then 2 else 1 end;
  -- Flags the difficulty under- or over-rates: the Nazi-era flag is among the best known of all; Weimar, the Dutch East
  -- Indies, the 1917 Provisional Government and Florence are known to anyone who has read the period; the rest are
  -- obscure outside their own country.
  update public.flag_color_match_items set fame = 3 where id in ('flag_002');
  update public.flag_color_match_items set fame = 2 where id in ('flag_005', 'flag_137', 'flag_193', 'flag_009');
  update public.flag_color_match_items set fame = 1 where id in ('flag_040', 'flag_360', 'flag_389', 'flag_311', 'flag_312', 'flag_292', 'flag_131', 'flag_141');
  -- Left out of the Daily Battle on purpose (2026-10-19, after looking at every flag rated 2 or 3):
  --  * the picture is the wrong flag for the name: 079 Irish Free State (a naval ensign), 093 Weimar war ensign (it is the
  --    Nazi flag), 104 People's Republic of China (the China Fire and Rescue flag), 143 Transvaal (not the Vierkleur), 149
  --    Belgian Congo (the 2006 DR Congo flag), 202 Sweden's union flag (a Danish customs flag), 478 Commonwealth of the
  --    Philippines (the President's flag), 492 People's Republic of Poland (the Luhansk People's Republic flag), 495 Latvia
  --    (the Latvian Soviet republic's flag) and 496 Estonia (the Estonian Workers' Commune's flag);
  --  * the symbol of a regime the highlighted mode should not put in front of everyone: 002 Nazi Germany, 023 Italian Social
  --    Republic, 039 Independent State of Croatia.
  -- The same look at every flag rated 1 found nine more wrong pictures that needed no change here (already fame 1): 068
  -- First Austrian Republic (a Silesian shield), 112 Mandatory Palestine (a Zionist flag), 151 French Equatorial Africa (a red
  -- crescent flag with a French canton), 247 Democratic Republic of Georgia (a red saltire), 264 Anglo-Egyptian Sudan (today's
  -- Sudan), 276 French Somaliland (a copy of 275 British Somaliland), 281 Portuguese Macau (today's Macau), 362
  -- Congo-Leopoldville (today's DR Congo) and 391 United Arab States (the UAE flag). They still appear in Officer-of-Arms
  -- itself; replacing them means new Wikimedia Commons images, which need the owner's permission first.
  update public.flag_color_match_items set fame = 1
    where id in ('flag_002', 'flag_023', 'flag_039', 'flag_079', 'flag_093', 'flag_104', 'flag_143', 'flag_149', 'flag_202', 'flag_478', 'flag_492', 'flag_495', 'flag_496');

  update public.territory_compare_items set fame = case difficulty when 1 then 3 when 2 then 2 else 1 end;
  -- The 1947 Kingdom of Greece and the 1850 Austrian Empire are rated 1 (easy) in the bank but are not household names.
  update public.territory_compare_items set fame = 2 where id in ('territory_180', 'territory_176');

  update public.true_false_items set fame = case difficulty when 1 then 3 when 2 then 2 else 1 end;
end
$fame$;
