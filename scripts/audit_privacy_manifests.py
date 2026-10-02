"""Applies App Store Connect's ITMS-91064 rule to every PrivacyInfo.xcprivacy inside a built .app.

Apple documents only one direction (tracking domains require NSPrivacyTracking = true), but it also
rejects an app-level manifest that sets NSPrivacyTracking = true with no domains at all, and that
shows up only after Add for Review as "Invalid Binary" - so both are enforced for the app's own file.
"""
import plistlib
import sys
from pathlib import Path

app = Path(sys.argv[1])
manifests = sorted(app.rglob("PrivacyInfo.xcprivacy"))
if not manifests:
    print(f"::error::No PrivacyInfo.xcprivacy found under {app}")
    sys.exit(1)

problems = []
for path in manifests:
    manifest = plistlib.loads(path.read_bytes())
    relative = path.relative_to(app).as_posix()
    tracking = manifest.get("NSPrivacyTracking") is True
    domains = manifest.get("NSPrivacyTrackingDomains") or []
    print(f"{relative}: NSPrivacyTracking={tracking} NSPrivacyTrackingDomains={len(domains)}")
    if domains and not tracking:
        problems.append(f"{relative} lists tracking domains but NSPrivacyTracking is not true")
    if relative == "PrivacyInfo.xcprivacy" and tracking and not domains:
        problems.append(
            f"{relative} sets NSPrivacyTracking true without NSPrivacyTrackingDomains; set it to false "
            "(listing AdMob's domains would make iOS block ad requests whenever ATT is declined)"
        )

for problem in problems:
    print(f"::error::ITMS-91064: {problem}")
sys.exit(1 if problems else 0)
