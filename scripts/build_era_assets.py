#!/usr/bin/env python3
"""Copies the owner's era art into the app's resources, following the owner's file naming.

    python scripts/build_era_assets.py [path to the eras folder]     (needs Pillow: pip install pillow)

The source folder (default D:/code_projects/historytrivia/eras) is the owner's and is only ever read. Per era folder
(ancient, medieval, early_modern, modern, contemporary) and per game (annalist, herald) it looks for
    <prefix>_<game>_logo.png   the game's card picture: colourful, with a transparent background
    <prefix>_<game>.png        the picture behind the game screen: black line art
plus one 06battle_map_all.png for Chancellor's card in every era. Each picture is checked before it is used, and
anything that is missing or does not look right is reported and left out (the app then shows its all-time artwork):
a logo with a baked-in opaque background would draw as a solid box on the card, and a colour picture in a
background slot would not read as a watermark. A logo may also be found under the background's name.

Logos are copied byte-for-byte. Backgrounds are tinted by the app and drawn as a faint watermark from their alpha
alone (see GameBackgroundArt), so any opaque white fill is turned into transparency first: new alpha = alpha x
(1 - luminance), colour black. Clean ink-on-transparent files come out unchanged.

Also compares the palette .txt files with Core/Theme/EraThemes.kt, which is where the colours live.
"""
import os
import re
import shutil
import sys

from PIL import Image, ImageChops

SRC = sys.argv[1] if len(sys.argv) > 1 else "D:/code_projects/historytrivia/eras"
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DST = os.path.join(REPO, "composeApp", "src", "commonMain", "composeResources", "files", "images", "eras")
ERA_THEMES = os.path.join(REPO, "composeApp", "src", "commonMain", "kotlin", "com", "konye", "historiq", "Core", "Theme", "EraThemes.kt")

ERAS = {"ancient": "ancient", "medieval": "medieval", "early_modern": "earmodern", "modern": "modern", "contemporary": "cont"}
GAMES = {"annalist": "year_guess", "herald": "casualties_multiplier"}
PALETTE_ORDER = ["Default background", "Annalist", "Herald", "Chancellor"]  # = EraPalette's base, yearGuess, casualtiesMultiplier, blindMapBattle
ERA_ENUM = {"ancient": "ANCIENT", "medieval": "MEDIEVAL", "early_modern": "EARLY_MODERN", "modern": "MODERN", "contemporary": "CONTEMPORARY"}

# Sepia-tinted line art measures up to ~0.16 and every colour illustration so far at least ~0.28.
COLOUR_THRESHOLD = 0.22

problems = []


def out(rel):
    path = os.path.join(DST, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    return path


def read(path):
    return Image.open(path).convert("RGBA")


def opaque_background(image):
    alpha = image.getchannel("A")
    w, h = alpha.size
    return all(alpha.getpixel(p) == 255 for p in ((0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)))


def mean_saturation(image):
    """Average saturation (0-1) of the visibly drawn pixels: low for line art, high for a colour illustration."""
    hsv = image.convert("RGB").convert("HSV")
    pairs = zip(image.getchannel("A").tobytes(), hsv.getchannel("S").tobytes())
    drawn = [s for a, s in pairs if a > 128]
    return (sum(drawn) / len(drawn) / 255) if drawn else 0.0


def is_logo(path):
    image = read(path)
    return not opaque_background(image) and mean_saturation(image) >= COLOUR_THRESHOLD


def is_line_art(path):
    image = read(path)
    return not opaque_background(image) and mean_saturation(image) < COLOUR_THRESHOLD


def build_logo(era, prefix, game_name, game_key):
    for name in (f"{prefix}_{game_name}_logo.png", f"{prefix}_{game_name}.png"):
        path = os.path.join(SRC, era, name)
        if os.path.exists(path) and is_logo(path):
            shutil.copyfile(path, out(f"{era}/logo_{game_key}.png"))
            print(f"logo {era}/logo_{game_key}.png <- {era}/{name}")
            return
    problems.append(f"{era} {game_name}: no usable logo (needs {prefix}_{game_name}_logo.png, colourful and with a transparent background)")


def build_background(era, prefix, game_name, game_key):
    name = f"{prefix}_{game_name}.png"
    path = os.path.join(SRC, era, name)
    if not os.path.exists(path):
        problems.append(f"{era} {game_name}: no background ({name} is missing)")
        return
    if not is_line_art(path):
        problems.append(f"{era} {game_name}: {name} is not line art (a colour picture or an opaque background), so it is not used as the background")
        return
    image = read(path)
    alpha = ImageChops.multiply(image.getchannel("A"), ImageChops.invert(image.convert("L")))
    result = Image.new("RGBA", image.size, (0, 0, 0, 0))
    result.putalpha(alpha)
    result.save(out(f"{era}/bg_{game_key}.png"), optimize=True)
    print(f"bg   {era}/bg_{game_key}.png <- {era}/{name}")


def check_palette(era, prefix):
    path = os.path.join(SRC, era, f"{prefix}_colorpalette.txt")
    if not os.path.exists(path):
        problems.append(f"{era}: no palette file")
        return
    text = open(path, encoding="utf-8", errors="replace").read()
    found = {}
    for label in PALETTE_ORDER:
        match = re.search(re.escape(label) + r":\s*#([0-9A-Za-z]{6})", text)
        if not match:
            problems.append(f"{era} palette: no colour for '{label}'")
            continue
        raw = match.group(1).upper()
        fixed = raw.replace("I", "1").replace("O", "0")  # the typos seen so far: a letter I or O typed for a 1 or a 0
        if not re.fullmatch(r"[0-9A-F]{6}", fixed):
            problems.append(f"{era} palette: '{label}: #{raw}' is not a colour code")
            continue
        if fixed != raw:
            problems.append(f"{era} palette: '{label}: #{raw}' read as #{fixed}")
        found[label] = fixed
    block = re.search(r"QuizEra\." + ERA_ENUM[era] + r" -> EraPalette\((.*?)\n        \)", open(ERA_THEMES, encoding="utf-8").read(), re.S)
    if not block:
        problems.append(f"{era}: not found in EraThemes.kt")
        return
    in_code = [c.upper() for c in re.findall(r"Color\(0xFF([0-9A-Fa-f]{6})\)", block.group(1))]
    expected = [found.get(label) for label in PALETTE_ORDER]
    if in_code != expected:
        problems.append(f"{era} palette differs from EraThemes.kt: file {expected}, code {in_code}")


for era, prefix in ERAS.items():
    for game_name, game_key in GAMES.items():
        build_logo(era, prefix, game_name, game_key)
        build_background(era, prefix, game_name, game_key)
    check_palette(era, prefix)

battle_map = os.path.join(SRC, "06battle_map_all.png")
if os.path.exists(battle_map) and is_logo(battle_map):
    shutil.copyfile(battle_map, out("06battle_map_all.png"))
    print("logo 06battle_map_all.png")
else:
    problems.append("06battle_map_all.png is missing or not a transparent logo")

print()
print("Nothing to report." if not problems else "Notes:")
for problem in problems:
    print(" -", problem)
