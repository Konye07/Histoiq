# Historiq

Kotlin Multiplatform + Compose Multiplatform app, Android and iOS from one codebase. Six daily
history-trivia quiz games, each playable once per day, resumable mid-round, with a real
friends/profile layer backed by Supabase. Material3 styling is shared identically across both
platforms — no native Cupertino reskin on iOS.

## Module layout

- `composeApp/` — the only app module. `src/commonMain` (screens, domain/data layer, navigation,
  string tables), `src/androidMain` (the installable Android app: `MainApplication`,
  `MainActivity`, real platform actuals), `src/iosMain` (real platform actuals, the
  `MainViewController()` entry point consumed by `iosApp/`).
- `iosApp/` — a thin SwiftUI shell (`iOSApp.swift`/`ContentView.swift`) hosting
  `ComposeUIViewController` via `MainViewController.kt`'s `MainViewController()` (exported to
  Swift as `MainViewControllerKt.MainViewController()`). The Xcode project itself
  (`iosApp.xcodeproj`) is generated from `iosApp/project.yml` by XcodeGen, not hand-authored or
  committed — `project.yml` is the source of truth, the generated project is gitignored. CI
  installs XcodeGen and runs `xcodegen generate` before building.
- The old single-module pre-KMP `app/` (this repo's original Android-only implementation) was
  deleted once the KMP rewrite's Android side reached full manual-regression parity — see git
  history before this point for that version if ever needed for reference.

## Build

- Gradle 8.11.1 via the wrapper (`./gradlew` / `gradlew.bat`) — no separate Gradle install needed.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`, AGP 8.10.0, Kotlin 2.3.20, Compose Multiplatform
  1.11.1 (not the newer 1.12.0 — that line's Android artifacts require compileSdk 37 + AGP 9,
  confirmed by a real `checkDebugAarMetadata` failure).
- Kotlin is pinned to 2.3.20, not older: an earlier pin (2.2.20) failed real iOS CI with a klib ABI
  mismatch — `org.jetbrains.compose.components:components-resources-iosArm64:1.11.1`'s klib was
  built by a 2.3.20 compiler, and a 2.2.20 compiler can only consume ABI <= 2.2.0. Gradle
  8.11.1/AGP 8.10.0 both remain compatible with 2.3.20 (compatibility range 8.2.2-9.0.0).
- iOS targets: `iosArm64` (device) + `iosSimulatorArm64` (Apple Silicon simulator) only — no
  `iosX64` (Intel simulator): current Compose Multiplatform/JetBrains-navigation artifacts publish
  no variant for it at all (confirmed via a real dependency-resolution failure). Virtually all
  current Macs are Apple Silicon, so this isn't a practical loss.
- Navigation is `org.jetbrains.androidx.navigation:navigation-compose` (JetBrains' own
  cross-published fork), not Google's `androidx.navigation:navigation-compose` — the latter's
  transitive `lifecycle-viewmodel-ktx` is Android/JVM-only and fails iOS resolution. Pinned to
  `2.9.0-beta03` specifically (contemporary with Compose 1.8.2) — `2.10.x` hits the same
  compileSdk-37/AGP-9 wall as Compose Multiplatform 1.12.0.
- supabase-kt is pinned to `3.3.0`, with Ktor at 3.3.3 to match. Don't move it without reading "Guest →
  account" under "Supabase backend": 3.2.5/3.2.6 break era-mode lookups and 3.8.0 needs Kotlin 2.4.0.
  Check a klib's `default/manifest` (`abi_version`) before bumping it or Ktor — iOS can't read an ABI
  newer than the compiler's.
- Supabase credentials come from `local.properties` (`SUPABASE_URL` / `SUPABASE_ANON_KEY`,
  gitignored) — `composeApp/build.gradle.kts` generates a `SupabaseConfig.kt` object from them at
  configure time (the multiplatform replacement for the old single-module app's `BuildConfig`
  field, since that mechanism doesn't exist for the iOS target).
- Release signing: inert until you generate a real keystore — `composeApp/build.gradle.kts` reads
  `RELEASE_STORE_FILE`/`RELEASE_STORE_PASSWORD`/`RELEASE_KEY_ALIAS`/`RELEASE_KEY_PASSWORD` from
  `local.properties` (same gitignored file as the Supabase keys) and only applies a release
  `signingConfig` when they're present; `*.jks`/`*.keystore` are also gitignored as a safety net.
  Without these, `:composeApp:bundleRelease` still runs but produces an unsigned .aab that Play
  Console will reject — generate the keystore before actually publishing.
- CI (`.github/workflows/ci.yml`), two jobs: `android` (ubuntu — `assembleDebug` +
  `compileIosMainKotlinMetadata`, the one iOS-adjacent check that doesn't need macOS) and `ios`
  (macos-latest — real `linkDebugFrameworkIosArm64`/`linkDebugFrameworkIosSimulatorArm64`, then
  XcodeGen + a simulator-only `xcodebuild` of `iosApp`, no signing certificate needed for that).
- No JDK 17+ is installed system-wide on the dev machine used so far — builds use a JDK 21 found
  under the user profile (`C:\Users\<user>\.jdks\...`) via `$env:JAVA_HOME`. This machine has no
  `git` on PATH by default either; it's at `C:\Program Files\Git\bin\git.exe`. It also cannot run
  the Kotlin/Native compiler for Apple targets at all (Kotlin/Native only links Apple binaries on
  macOS) — see "Verifying iOS platform code" below for how iOS-side changes get checked without one.

## iOS release pipeline (TestFlight / App Store, no Mac needed)

`.github/workflows/ios-release.yml` (manual `workflow_dispatch`, or any pushed `ios-release-*` tag, which
always uploads) links the release framework, archives
`iosApp` with manual Apple Distribution signing, exports an .ipa and uploads it to App Store Connect.
Signing is manual because automatic signing must create a development profile, which needs a
registered device, and this team has none.

- `IOS_SIGNING_BUNDLE` (GitHub Actions secret) is a base64 tar.gz of the distribution certificate
  (`.p12` + its password), the "Historiq App Store" provisioning profile, and the App Store Connect
  API key. `node scripts/ios-signing.mjs --key <AuthKey_XXXX.p8> --key-id <id> --issuer <uuid> --out
  <dir outside the repo>` regenerates all of it through the App Store Connect API (the key needs the
  Admin role). Key ID, Issuer ID and Team ID are plain `env` in the workflow, not secrets.
- The certificate and profile expire **2027-10-01**: rerun the script before then and replace the
  secret, or the run fails at the signing/export step.
- Build number = the workflow run number (`CURRENT_PROJECT_VERSION`), unless the tag is named
  `ios-release-build-<n>`, which uploads as build n (a failed run still uses up a run number, so the tag is the
  way to land on an exact build number; it must be above the last one in App Store Connect); the marketing version is
  `MARKETING_VERSION` in `iosApp/project.yml`, and it must match the version created in App Store
  Connect for a build to be selectable there.
- iOS AdMob unit IDs flow through `AdsConfig` from `local.properties` (`ADMOB_*_IOS`) and default to
  Google's test units. Both `ci.yml` (so the simulator zip exercises them) and `ios-release.yml` write
  the real ones, and `scripts/verify_ad_units.py` proves they are compiled into the built app. At
  runtime `AdsBridge.swift`'s `AdEnvironment` swaps in Google's test units for TestFlight and App
  Review installs (sandbox receipt), so testers never tap live ads, while App Store installs use the
  real units; the simulator keeps the real ones because the Mobile Ads SDK treats it as a test
  device. The AdMob app ID is in `Info.plist`.
- The app is iPhone-only (`TARGETED_DEVICE_FAMILY: "1"`): an iPad-capable build must support all four
  orientations or App Store Connect rejects it (ITMS-90474), and the UI is portrait-only.
- `PrivacyInfo.xcprivacy` declares the required-reason APIs the Kotlin/Native + Compose runtime uses.
  `NSPrivacyTracking` stays **false** even though the app shows the ATT prompt: App Store Connect
  rejects an app-level `true` that has no `NSPrivacyTrackingDomains` (ITMS-91064, only found at Add for
  Review, as "Invalid Binary"), and listing AdMob's domains would make iOS block ad requests for
  everyone who declines ATT. The ad SDKs declare their own tracking in their own manifests.
  `scripts/audit_privacy_manifests.py` enforces this in `ci.yml` and on the exported .ipa in
  `ios-release.yml`.

## Verifying iOS platform code without a Mac

Every `iosMain` `actual` that calls a real Apple framework (UIKit, Foundation, AVFAudio,
UserNotifications, ...) needs its exact Kotlin/Native binding signature confirmed before pushing —
guessing and waiting for CI wastes a real round-trip per mistake. The Kotlin/Native distribution
ships its platform klibs locally even on Windows:

```
~/.konan/kotlin-native-prebuilt-windows-x86_64-<version>/klib/platform/ios_arm64/org.jetbrains.kotlin.native.platform.<Framework>/
```

Run `klib dump-metadata <path-to-that-directory>` (the `klib`/`klib.bat` CLI ships in the same
distribution's `bin/`) and grep the output for the class/function in question — this gives the
*exact* generated Kotlin signature (parameter names, nullability, whether an `-init...` method is
a real constructor or has been deprecated to `ERROR` level in favor of a `ClassName.create(...)`
factory, which extension functions need their own import beyond the class's, etc.), not a guess
from Swift/Objective-C documentation. `compileIosMainKotlinMetadata` then actually type-checks
`iosMain` actuals against those real bindings (not just expect/actual signature matching) —
run it locally before pushing. The same technique works for any Compose Multiplatform library
klib (e.g. confirming `ComposeUIViewController`'s signature in `compose.ui`'s iosArm64 klib under
the Gradle cache) when the uncertainty is in a KMP library rather than a raw platform binding.

## Platform-specific code (expect/actual)

Real Android + real iOS implementations exist for: `Haptics`, `BubbleSoundPlayer` (shares its
waveform-generation math with Android via `commonMain/Core/Audio/BubbleWaveform.kt`, so both
platforms synthesize the identical sound from Float PCM samples), `ShareResults` +
`ShareCardRenderer` (the share-card image itself is rendered natively per platform — Android via
`android.graphics.Canvas`, iOS via `UIGraphicsBeginImageContextWithOptions` — and the boundary is
PNG bytes (`ShareCardRenderer.renderPng`), not a shared bitmap type, since there's no common way to
represent a platform bitmap across both), `ReminderScheduler`, `rememberRequestNotificationPermission`,
`openEmailComposer`, `copyToClipboard`, `openUrl`, `InterstitialAd`/`RewardedAd` (`Core/Ads/`),
`signInWithGoogle` (`Core/Auth/GoogleSignIn.*.kt`), and `signInWithApple` (`Core/Auth/AppleSignIn.*.kt` — iOS only;
Android's `actual` always returns `AppleAuthOutcome.NotImplementedOnThisPlatform` since the button
itself is hidden there, see `showAppleSignInButton`). All live under
`Core/{Haptics,Audio,Share,Notifications,Platform,Ads,Auth}/` with `.android.kt`/`.ios.kt` suffixes
alongside the common `expect` declaration.

Anything that hands off to another app must cope with "nothing can handle this": an Android
`startActivity` with no match throws `ActivityNotFoundException` and kills the app (a phone with no
mail app crashed on the Play tab's suggestion panel), and iOS's old one-argument `openURL` fails
silently, so `openEmailComposer` takes an `onUnavailable` callback and uses the completion-handler
form of `openURL`. Contact-email controls go through `rememberEmailContact()` (`Core/Platform/
EmailContact.kt`): tapping opens the mail app, and with none it copies the address to the clipboard
and shows a confirmation for a moment. Don't call `resolveActivity` first on Android: without a
`<queries>` entry it reports no mail app even when one is installed.

Two different iOS integration strategies show up depending on the SDK. `signInWithApple` calls
`AuthenticationServices` directly via Kotlin/Native cinterop, since it's a first-party Apple
framework with a real module map. AdMob and Google Sign-In are both SPM-only distributions with no
C/Objective-C module map for cinterop to bind against, so their real SDK calls instead live in
native Swift (`iosApp/iosApp/AdsBridge.swift`, `GoogleSignInBridge.swift`), each implementing a
plain callback-based Kotlin interface (`AdsBridge`, `GoogleSignInBridge` in
`Core/Platform/IosNativeBridges.kt`) registered once at app launch in `iOSApp.swift`'s `init`. The
SPM packages themselves are declared in `iosApp/project.yml`, not a Gradle file — currently
`GoogleSignIn` (from `10.0.0`) and `GoogleMobileAds` (from `13.9.0`).

## String localization

`LocalAppLanguage` (a `compositionLocalOf<AppLanguage>`, deliberately *not* Compose Multiplatform's
built-in resource-string locale system — that resolves against the OS/system locale and doesn't
reliably support an in-app runtime override on iOS, per JetBrains' own tracker CMP-8376) paired
with `LocalOnLanguageChange` (a setter callback provided at the same site) so Settings' language
picker can update the whole app instantly, with no restart, without threading a callback through
every intermediate screen/nav-host signature — both are provided together in `App.kt`. Actual
strings live in generated-by-script `Strings*.kt` objects per topic under `Core/Localization/`,
one Kotlin property/function per key, `@Composable get() = when (LocalAppLanguage.current) { ... }`
for translated strings or a plain `const val` for English-only content (e.g. the privacy policy).

Six languages: English, Spanish, French, German, Italian, Hungarian (`AppLanguage.HU`, added 2026-10-11; the
`when` over the enum is exhaustive, so a new string with a missing branch fails to compile). Hungarian uses the
informal "te" imperative, „…” quotes, and the in-app names Évkönyvíró, Hírnök, Címertiszt, Kozmográfus,
Krónikás, Kancellár. Quiz content is translated in the database, not in the app: `true_false_items` has
`statement_es/fr/de/it/hu` (`TrueFalseStatement.localized`, English when a column is null) and every other bank
has Hungarian `_hu` columns (see "Supabase backend"); Spanish, French, German and Italian players get translated
names and hints for the three dated banks, and English descriptions, flag names and countries; the privacy policy and the tutorial screenshots stay English in every language. A
new true/false statement needs all five translations in the same insert, or that language shows English for it.
Years are written by `formatEraYear` (`Core/Localization/EraYear.kt`): the era label follows the language, and
Hungarian puts it first ("Kr. e. 480").

## Supabase backend

Project "HistoryTrivia" (`xolybjypkkpeymsvfimr`). `Data/Remote/SupabaseClient.kt` installs
`Postgrest` and `Auth`; the Ktor engine is swapped per platform (`ktor-client-android` /
`ktor-client-darwin`), everything else in this file is common code.

**Auth**: every device signs in anonymously first (`client.auth.signInAnonymously()`) to get a
real, verifiable `auth.uid()`. "Continue with Google" is real OAuth on both platforms — Android via
Credential Manager (`GoogleSignIn.android.kt`), iOS via the native Google Sign-In SDK through a
Swift bridge (`GoogleSignIn.ios.kt`, see "Platform-specific code" above) — and links a real Google
identity token to that same anonymous session rather than replacing it (see "Guest → account"
below), so any guest-mode progress carries over. "Continue with Apple" is real
too, but iOS-only: native `AuthenticationServices` via direct cinterop (`AppleSignIn.ios.kt`) — the
button is hidden entirely on Android (`showAppleSignInButton`), where `signInWithApple` is
structurally unreachable. `AuthProvider.kt`'s `mockSignIn` still exists but is now only a defensive
fallback for the `NotImplementedOnThisPlatform` outcome branches in `AccountRepository.kt` — not
the live path behind either real button anymore. Guest mode gets this same anonymous session too,
via `AccountRepository.ensureAnonymousSession()` — it only skips attaching a display identity. So a
guest is *not* fully local: `score_history`/`quiz_progress` still write to Supabase under that
anonymous `auth.uid()` (see the schema note below); only the `profiles` row, `game_stats` lifetime
aggregates, and Friends are gated to a real attached identity. Apple's Guideline 4.8 (offer Sign in
with Apple if any other third-party login is offered) is now genuinely satisfied on iOS, not just
structurally — both buttons are real OAuth there.

**Guest → account**: Google (both platforms) and Apple go through `linkOrSignInWithIdToken`
(`Core/Auth/IdTokenAuth.kt`), which first tries `auth.linkIdentityWithIdToken`. That attaches the
identity to the device's anonymous user, so its `auth.uid()` — and with it the guest's
`score_history`, `quiz_progress` and streak — simply becomes the account's. Facebook does the same
with `auth.linkIdentity(Facebook)` (browser OAuth, `FacebookOAuthSession.kt`).
- *Library version*: this only works since supabase-kt **3.3.0**. Up to 3.2.4 the request body
  carried `linkIdentity` instead of `link_identity`, Supabase Auth ignored it, and every sign-in made
  a brand-new user (found 2026-10-05: no Google or Apple identity had ever been linked to an
  anonymous user). The pin comment in `composeApp/build.gradle.kts` explains why 3.3.0 and not
  3.2.5/3.2.6 (they quote top-level filter values, so `eq("game_key", "year_guess:ancient")` matches
  nothing) or 3.8.0 (built with Kotlin 2.4.0, unreadable by our 2.3.20 on iOS). To check linking in
  production, `linked_after_anon` should rise above 0 for google/apple once the fixed build is out:
  ```sql
  select i.provider,
         count(*) filter (where i.created_at - u.created_at > interval '2 seconds') as linked_after_anon,
         count(*) filter (where i.created_at - u.created_at <= interval '2 seconds') as direct_signup
  from auth.users u join auth.identities i on i.user_id = u.id
  where u.is_anonymous = false group by i.provider;
  ```
- *Identity already belongs to another account* (new phone, reinstall, sign-out and back in): the
  link fails with `identity_already_exists` and the helper falls back to a plain `signInWith(IDToken)`
  as that account (Facebook: `signInWith(Facebook)`). The swap makes the guest's rows unreadable
  (owner-only RLS), so `GuestProgress` (`Data/Quiz/GuestProgress.kt`) reads them just before it and
  copies them across right after: every `score_history` row plus today's `quiz_progress`, inserted
  `ON CONFLICT DO NOTHING`. The account's own rows win; the guest only fills gaps (a round played
  today, past days for the streak and best score). It is best-effort — a failure is logged, never
  fails the sign-in — and runs inside `AccountRepository`'s session lock, so it must not call
  `ensureAnonymousSession()`. The abandoned anonymous user and its rows stay (a client can't delete
  them) and count in that day's `get_final_scores` like any other guest. Never run the Supabase
  docs' "delete anonymous users older than 30 days" cleanup: every FK to `auth.users` cascades, so it
  would also erase the progress of long-lived guests.
- *Dashboard setting*: Authentication → Sign In / Providers → "Allow manual linking" must be on
  (confirmed on 2026-10-05). It gates the `/user/identities/*` routes that Facebook's `linkIdentity` uses (with it off the call
  throws and Facebook silently falls into the plain sign-in path); Supabase's docs also list it as
  required for converting an anonymous user, although the `id_token` grant that Google/Apple use
  isn't gated by it in Supabase Auth's source.
- *Android resume race* (found 2026-10-11 from a `game_stats` RLS error in the logs): supabase-kt's Android
  lifecycle hook sets the session status to `Initializing` when the app goes to the background and, on return,
  reloads the stored session and calls `initDone()` straight away — flipping it to `NotAuthenticated` while the
  refresh of a token past 80% of its life is still on the wire. `ensureAnonymousSession()` used to take that for
  "no session", signed in anonymously (which cancels the refresh and overwrites the stored session) and so turned a
  signed-in player into a new empty guest; their next launch showed the sign-in screen. It fired when a results
  screen re-ran `recordAndRank` on resume after the app had been away for ~48+ minutes. iOS has no such hook.
  `ensureAnonymousSessionLocked` now waits for a session that is still in storage and throws (callers retry)
  instead of replacing it, and `AccountRepository.signedInAccount()` — used by `StatsStore` and `bestFor` — returns
  the cached profile only while the live session is that account's, so a mismatch can't reach RLS. To find past
  victims: an anonymous user whose first `auth.refresh_tokens` row falls within ~1.5 s of another user's rotated
  token (three before the fix). They recover by signing in again (`GuestProgress` carries the stray rows over).

**Schema** (all in `public`, RLS enabled on every table):
- `profiles` — one row per `auth.uid()`, mirrors `UserAccount`. Friend-code lookup by another user
  goes through the `lookup_friend_code(code)` SECURITY DEFINER function (never a direct SELECT —
  that would leak emails to strangers), which is rate-limited (20 lookups/hour per caller) via
  `friend_code_lookup_attempts` (`caller_id`, `attempted_at`) — RLS enabled, no policies, same
  zero-direct-client-access shape as `device_audit_log` below. An AFTER DELETE trigger
  (`delete_auth_user_after_profile_delete`) also deletes the `auth.users` row, so the app's Delete
  Account (whose last step is deleting the profile) removes the email and provider id too, without a
  client update; a client can't delete an auth user itself.
- `friendships` — real friend requests/acceptances between real accounts (`FriendsRepository.kt`,
  `FriendsStore.kt`). Replaced the old hardcoded `FriendDirectory` demo directory and
  `demoScoreFor`'s synthetic scores entirely — deleted, not kept as fallback.
- `game_stats`, `score_history`, `quiz_progress` — one row per `(user_id, game_key[, date_key])`,
  owner-only RLS except `score_history`'s SELECT policy, which also allows an *accepted friend* to
  read it (that's what makes the Friends leaderboard real). Only `game_stats` (`StatsStore`) is
  actually gated to real accounts (`AccountRepository.currentAccount() == null` short-circuits to a
  no-op/empty result). `score_history`/`quiz_progress` write under *any* device's anonymous
  `auth.uid()` — guest or signed-in alike, via `AccountRepository.ensureAnonymousSession()` — so a
  guest's daily score/progress is just as real server-side as a signed-in account's: it counts
  toward the same day's rank/"total players" (`get_final_scores`, below) alongside every signed-in
  player, and "once per game per day" is server-enforced for a guest too, not just a device-local
  flag. What a guest never gets is a `profiles` row, lifetime `game_stats`, or Friends — those three
  specifically require a real attached display identity.
- `daily_quizzes` — the fix for "same quiz everywhere on a given date": `(game_key, date_key) ->
  item_ids`, publicly readable, writable only through `get_or_create_daily_quiz(game_key, date,
  count)` (SECURITY DEFINER). That function is the *only* place the cross-day cooldown logic
  exists — it excludes ids used within the game's cooldown window per `daily_quizzes` history (15
  days by default, shorter for a few games — see the function's own comment), falls back
  to the unfiltered pool if too few remain, and atomically inserts-once/reads-after so every device
  gets the identical set. Territory Compare (a continuous survival queue, not a fixed N) asks for a
  larger pool (60 ids) and turns it into match order client-side with a `Random` seeded by the
  date, so the *shuffle* is deterministic and shared too, not just the item set. A game key may
  also be an era mode key, `<game>:<era>` (see "Era game modes" below): the function then draws only
  that era's questions and excludes a question for just 2 days.
- `year_guess_items`, `casualties_multiplier_items`, `flag_color_match_items`,
  `territory_compare_items`, `true_false_items`, `blind_map_battle_items` — the question banks,
  1:1 with the Kotlin data classes in `Data/Quiz/`, publicly readable, no client insert policy
  (content is team-maintained via SQL/dashboard). `QuizDataSource.kt`'s loaders take the specific
  id list to fetch (from `get_or_create_daily_quiz`), not "load everything." Images stay bundled
  as `commonMain/composeResources/files/images/...` (`imageAsset` is still a plain path relative to
  that root) — only the structured text/numeric data moved to Postgres, not the flag/battle PNGs.
  `blind_map_battle_items.year` (NOT NULL) is never read by the client — it dates that bank like the
  other two (`correct_year` / `year`) so every question can be flagged with its era; give every new
  battle one. Those three banks also carry `era_key`, a stored generated column (see "Era game modes");
  it is not the free-text `era` label that `territory_compare_items` has.
  Adding questions: keep an idempotent audit copy in `supabase/question_banks/` (`insert ... on conflict
  (id) do nothing`; ids continue the numbering, `event_NNN` / `battle_NNN` / `battlemap_NNN`), dry-run it
  on the scratch Postgres, then check the per-era counts. `correct_casualties` is killed + wounded of both
  sides, midpoints of ranges, prisoners excluded, and the figure must agree between a Wikipedia infobox and
  the article's own text (several don't, e.g. Breadfield, Mühlberg, Belgrade 1717); the row's `source` says
  how it was combined. Chancellor needs only a year and coordinates, so a battle with no usable casualty
  figure (Vajkal, Ugra) can still go there. An Annalist event name must not contain its year.
  `year_guess_items.description` and `casualties_multiplier_items.description` (nullable text) hold a 2-4
  sentence write-up of what happened and why it matters, English only, shown under the answer in the
  reveal panel and in the results list (`QuestionDescription` in `Features/Play/Shared/ResultsComponents.kt`;
  a row without one draws nothing, and builds from before the column existed ignore it). Give every new
  Annalist event and Herald battle one in the same insert; Chancellor has none. One text serves the Annalist
  event and the Herald battle with the same name and year, so keep the two identical:
  `supabase/question_banks/20261006_descriptions_<era>.sql` set them by (name, year) through a temp table.
  Write each in your own words from the Wikipedia article, say only what the article supports, and keep it
  consistent with the stored answer (the Herald figure for Verdun, Gallipoli, Petersburg and the like covers
  the whole campaign, not one day). The 1858 "Battle of Monterrey (Reform War)" row has none on purpose: its
  own `source` says no such battle could be found.
  **Hungarian**: every bank row carries its Hungarian text in `_hu` columns next to the English one
  (`year_guess_items.event_hu/hint_hu/description_hu`, `casualties_multiplier_items.battle_hu/hint_hu/
  description_hu`, `blind_map_battle_items.battle_hu`, `flag_color_match_items.flag_name_hu`,
  `territory_compare_items.country_hu/era_hu`), migration `20261011100000_question_banks_hungarian.sql`; the
  client shows them through each data class's `localized…(languageCode)` and falls back to English where a
  column is null. Spanish, French, German and Italian have only names and hints (`event_es/fr/de/it`,
  `battle_…`, `hint_…` on `year_guess_items`, `casualties_multiplier_items` and `blind_map_battle_items`,
  migration `20261011110000_question_banks_names_hints_es_fr_de_it.sql`), filled the same way, by English text,
  so give a new row those too; `true_false_items` has its own five. Every new row needs
  its `_hu` values in the same insert. Keep the Hungarian of one name identical across the three banks (the
  same English name or hint was translated once and applied by text to every table that has it) and do not
  put the year in an Annalist `event_hu` either. The translations live only in the database, not in
  `supabase/question_banks/`; `territory_compare_items.era_hu` is derived from `era` ("1920 (peak)" ->
  "1920 (csúcs)", "301 BCE (peak)" -> "Kr. e. 301 (csúcs)").
- `device_audit_log` — sign-in security audit trail (IP, user agent, event, timestamp). **No RLS
  policies at all** — zero client access, not even the account's own row. Only reachable through
  `log_device_event(event)` (SECURITY DEFINER), which reads the caller's IP from PostgREST's
  `request.headers` so the client can never spoof it. Disclosed in the Privacy Policy screen/page.
- `get_final_scores(game_key, date)` / `get_question_guesses(game_key, date, question_id)` — both
  SECURITY DEFINER, both return only anonymized aggregate values (never which user), because
  `score_history`/`quiz_progress`'s RLS is otherwise owner/friend-only by design. This is what
  `DailyDistribution.kt` calls for the results-screen rank display and per-question
  guess-distribution axes; "total players today" is simply the length of `get_final_scores`' real
  result, not a synthetic ramp.

**When adding a new query that needs "everyone's data" for some anonymized aggregate**: don't
widen a table's RLS policy to make it work — add a narrow SECURITY DEFINER function that returns
only the aggregate, following the two examples above. Run the Supabase security/performance
advisors after any schema change and check each finding's actual SQL body before assuming it's a
real problem — this project's own SECURITY DEFINER functions intentionally trip the
"callable by anon/authenticated" and "RLS enabled, no policy" lints by design (verified 2026-09-12:
every current finding is the narrow-aggregate/write-only-audit pattern above, not a leak).

**Reading Supabase logs**: every log read counts the GB it scans against the organization's monthly
Logs Query quota — `query_logs`, the Management API and Studio's Logs and Reports pages alike. This
project writes only about 50 log events per 10 minutes (roughly 6 MB a day across all sources,
measured 2026-10-08 when the meter already read 15 of 100 GB), so a high number means many reads,
not big data. Try `execute_sql` first: current state lives in tables (`auth.users`,
`auth.identities`, `device_audit_log`, ...). When the logs really are needed, scope the window to a
few minutes around a known timestamp (never the 24 h default), query one `source =` at a time,
select named `log_attributes['key']` values (never the whole map), filter on a field rather than
`event_message like '%...%'`, and never call it in a loop or on a timer.

## The six games

| Role name (in-app) | GameKeys constant | Mechanic |
|---|---|---|
| Annalist | `YEAR_GUESS` | Guess the year of an event/battle (1000-point scale per question, `yearGuessScore`) |
| Herald | `CASUALTIES_MULTIPLIER` | Guess a battle's casualty count; score is a multiplier, lower is better |
| Officer-of-Arms | `FLAG_COLOR_MATCH` | Recolor a historical flag's dominant hue/sat/brightness to match the original |
| Cosmographer | `TERRITORY_COMPARE` | Pick the bigger of two countries by peak territorial size; sudden-death, 10 rounds |
| Chronicler | `TRUE_FALSE` | True/false historical statements; sudden-death, 10 rounds |
| Chancellor | `BLIND_MAP_BATTLE` | Tap a battle's location on a blank world map (1000-point scale per question, `mapPinpointScore`) |

`GameKeys.kt` (`Data/Quiz/`) is the single source of truth for each game's key, role name, title,
and color — screens must reference `GameKeys.X`, never re-declare the key as a local string
literal. Each game's key also names its Postgres question-bank table (`<key>_items`) and is the
`game_key` column value everywhere else — keep them in sync if a key ever changes.

## Era game modes

Annalist, Herald and Chancellor (`GameKeys.ERA_FILTERABLE` — the only banks whose questions carry a
year) each have five more game modes, one per `QuizEra`: ancient (before 476 CE), medieval (476–1452),
early modern (1453–1750), modern (1751–1899), contemporary (1900+). The boundaries live only in the
`era_for_year` Postgres function, and every question in those three banks carries its era as a stored
generated column, `era_key` — derived from its date, filled in for new questions automatically, and an
era mode recalls its questions with `era_key = '<era>'`. (One column rather than five 1/0 flags: a
question is in exactly one era.) Change the boundaries and the stored flags are refreshed by
rewriting the date column (`update <bank> set <date column> = <date column>`). The app always opens
in All-time, the original six games; the Play
screen's `EraModeStrip` switches `ActiveEra` (in memory, never persisted) and the whole screen becomes
that era's own mode — its own palette, its own pictures, only those three games. It is a mode you step
into, not a filter on the original.

- **Keys**: an era mode is the game key `GameKeys.modeKey(game, era)` = `"<game>:<era>"`
  (`year_guess:ancient`). Every table and function keyed by `game_key` (`daily_quizzes`,
  `quiz_progress`, `score_history`, `game_stats`, `get_final_scores`, `get_question_guesses`) treats it
  as a game of its own, so an era mode has its own daily round (once a day, resumable), streak
  (`StreakStore.computeAll` is keyed by mode key), rank, ads, lifetime stats and Friends leaderboard
  with no schema change there. `GameKeys.roleName/title/color/formatScore` accept a mode key and
  resolve it to its base game (`title` appends "(Era)"). Always build one with `modeKey`, never by
  string concatenation.
- **Screens**: a game screen reads `ActiveEra.current()` once when it opens and passes the resulting
  mode key to everything keyed by game (resume, persist, rank, guess distributions, `ResultsHeader`).
  Friends and Profile have their own local era filter (`EraFilterChip`), starting on the mode you are
  in; picking an era lists only the three era games.
- **Server**: `supabase/migrations/20261004120000_era_filtered_daily_quizzes.sql` adds
  `blind_map_battle_items.year`, `era_for_year`, the `era_key` flag on the three banks and the era
  branch of `get_or_create_daily_quiz`. It is additive (builds without era modes keep working) and
  idempotent. Applied to the live project on 2026-10-04 (migration `era_filtered_daily_quizzes`) after
  testing against a scratch Postgres, and verified end to end with a full Annalist, Herald and
  Chancellor era round on an Android device; without it an era round sits on its loading screen,
  retrying. Questions added later are flagged automatically from their date.
- **Look**: `EraThemes` (`Core/Theme/EraThemes.kt`) holds each era's palette, taken from the owner's
  `<era>_colorpalette.txt` files (two typos read as `#5E1C23` and `#A0822D`). Pictures go in
  `composeResources/files/images/eras/<era key>/`: `banner.png` (optional header on the era's Play
  screen), `logo_<game key>.png` (the game cards) and `bg_<game key>.png` (the watermark behind each
  game screen, drawn whole and centered: Herald lifts only the wide all-time one, because the tall era
  drawings fill the screen's box and lose their top when raised); Chancellor's card is
  `images/eras/06battle_map_all.png` for every era. A missing picture
  falls back to the all-time artwork. Do not copy the owner's folder by hand: run
  `python scripts/build_era_assets.py` (needs Pillow), which maps their files by name, rejects a logo with an
  opaque background or a colour picture in a background slot, turns the line art's opaque white fills
  into transparency (the app tints backgrounds from alpha alone) and reports what is missing.
  Results colours: the success green is passed through `legibleOn` so it stays readable on every palette.
  **Dark mode**: the owner supplies only light palettes; `EraThemes.palette(era, dark = true)` derives each
  era's after-dark palette (`EraPalette.forDarkMode`): hues kept, backgrounds a deep tint (an era whose light
  background is lighter stays a little lighter, and the tint is floored, because plain darkening turns the
  pale grayish eras — Ancient, Modern, Contemporary — into the same charcoal), game colors pulled into a dark
  band just above the background. `GameKeys.color(key, dark)` and every era call site (Play, the three game
  screens, the bottom bar, Friends, Profile) pass `LocalIsDarkTheme.current`; the all-time games keep their
  fixed signature colors in dark mode. Tune `EraPalette`'s `DARK_*` constants, not per-era values.

## Onboarding tutorial

`Features/Onboarding/TutorialScreen.kt`, shown once per device (`SettingsStore.hasSeenTutorial`, gated in
`RootApp`) and skippable at any page. Ten pages: welcome, the six games, two era pages, closing. The game pages
are real on-device screenshots (`composeResources/drawable/tutorial_*.png`), so the text inside them stays English
in every language; the three games with era modes carry an "Also in 5 historical eras" badge. The era pages
(`TutorialEraPages.kt`) are drawn live from `QuizEra.entries`, `EraThemes` and the app's own strings, so they follow
the language, dark mode and text size and need no edit when an era's palette or picture changes; the second one
cycles through the eras while it is on screen. When a game gains or loses a feature, update its page text in
`StringsTutorial.kt` (five languages) and re-take its screenshot. Because the tutorial runs once, a player who
finished it before a change never sees that change; Settings has no "replay tutorial" row yet. To re-run it on a
device, clear the app's data (or `hasSeenTutorial` in `app_settings`).

## Fixed color palette

Each game gets its own Material3 `ColorScheme` built from one base color via `gameColorScheme()`
in `Core/Theme/Theme.kt` (`AppColors` object holds the palette). Don't introduce new colors outside
this palette without asking. Era game modes are the exception: each era is meant to get a palette of
its own (see `EraThemes`), supplied by the project owner.

## Conventions

- Text sizes come from `MaterialTheme.typography`, never a literal `sp`/`fontSize`: Settings → Text size
  (`AppFontSize` in `Core/Theme/AppFontSize.kt` — Small 85%, Medium 100% = the original look and the default,
  Large 125%, on top of the device's own text size) scales the typography that `AppTheme` provides, and a
  hard-coded size would silently ignore it. It scales the styles rather than the density's fontScale because a
  dialog is its own window with a fresh density, so a density override would leave dialogs at the old size.
  The share-card images are drawn natively at a fixed size on purpose.
- No comments unless the *why* is genuinely non-obvious (a workaround, a subtle invariant). Never
  comments that just restate what a well-named identifier already says.
- Historical images/flags: only ever sourced from Wikimedia Commons with a verified public-domain
  or CC license, downloaded after explicit permission each time. Never fabricate or scrape from
  sources with unclear licensing.
- True/false statements and other bulk trivia content must be genuinely distinct facts — an earlier
  batch turned out to be ~90 real facts reworded into 1700 near-duplicates via templated lead-in
  phrases, which defeats the 15-day repeat cooldown. Dedupe by underlying fact, not by exact string.
- Guest mode (no account) is real Supabase-backed for day-scoped data (score/progress/rank — see
  the Auth section above), not "mock-now/local-only" — the only things it deliberately lacks are a
  profile, lifetime stats, and Friends (no portable credential to sync a second device against, by
  design), not a placeholder for something not yet built. Everywhere else, prefer the real
  Supabase-backed path; don't reintroduce a synthetic/local fallback for a signed-in account.
