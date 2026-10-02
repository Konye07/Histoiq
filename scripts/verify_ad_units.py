"""Confirms the iOS AdMob unit IDs listed in local.properties are compiled into a built .app.

Kotlin/Native stores string constants as UTF-16, so that is the encoding searched for. Debug builds keep
the code in a <name>.debug.dylib next to a stub executable, so every Mach-O in the app's root is searched.
"""
import plistlib
import sys
from pathlib import Path

GOOGLE_TEST_UNITS = {
    "ADMOB_INTERSTITIAL_UNIT_ID_IOS": "ca-app-pub-3940256099942544/4411468910",
    "ADMOB_REWARDED_UNIT_ID_IOS": "ca-app-pub-3940256099942544/1712485313",
}

app, properties = Path(sys.argv[1]), Path(sys.argv[2])

configured = {}
for line in properties.read_text().splitlines():
    key, _, value = line.partition("=")
    if key.strip() in GOOGLE_TEST_UNITS:
        configured[key.strip()] = value.strip()
unset = [key for key in GOOGLE_TEST_UNITS if not configured.get(key)]
if unset:
    print(f"::error::{properties} has no value for {', '.join(unset)}")
    sys.exit(1)

executable = plistlib.loads((app / "Info.plist").read_bytes())["CFBundleExecutable"]
code = b"".join(path.read_bytes() for path in [app / executable, *sorted(app.glob("*.dylib"))])


def compiled_in(unit_id):
    return unit_id.encode("utf-16-le") in code


failed = False
for key, unit_id in configured.items():
    test_unit = GOOGLE_TEST_UNITS[key]
    if compiled_in(unit_id):
        print(f"{key}: {unit_id} is compiled in")
    elif compiled_in(test_unit):
        print(f"::error::{key}: {unit_id} is NOT compiled in, but Google's test unit {test_unit} is, so this build would serve test ads")
        failed = True
    else:
        print(f"::warning::{key}: {unit_id} was not found as UTF-16 text, so it could not be verified")
sys.exit(1 if failed else 0)
