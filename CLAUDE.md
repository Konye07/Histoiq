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
`openEmailComposer`, `openUrl`. All live under `Core/{Haptics,Audio,Share,Notifications,Platform}/`
with `.android.kt`/`.ios.kt` suffixes alongside the common `expect` declaration.

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

## Supabase backend

Project "HistoryTrivia" (`xolybjypkkpeymsvfimr`). `Data/Remote/SupabaseClient.kt` installs
`Postgrest` and `Auth`; the Ktor engine is swapped per platform (`ktor-client-android` /
`ktor-client-darwin`), everything else in this file is common code.

**Auth**: every device signs in anonymously (`client.auth.signInAnonymously()` — no real
Google/Apple OAuth credentials exist) to get a real, verifiable `auth.uid()`. "Continue with
Google/Apple" (`AuthProvider.kt`'s `mockSignIn`) just attaches a display identity (provider, mock
email/name) to that one real session — see `AccountRepository.kt`. Guest mode gets this same
anonymous session too, via `AccountRepository.ensureAnonymousSession()` — it only skips attaching
a display identity. So a guest is *not* fully local: `score_history`/`quiz_progress` still write to
Supabase under that anonymous `auth.uid()` (see the schema note below); only the `profiles` row,
`game_stats` lifetime aggregates, and Friends are gated to a real attached identity. Note for an
eventual real App Store submission: Apple's Guideline 4.8 requires offering Sign in with Apple if
any other third-party login is offered — structurally already satisfied since Apple is one of the
two options here, but both need to become real OAuth (not mock) before that actually counts.

**Schema** (all in `public`, RLS enabled on every table):
- `profiles` — one row per `auth.uid()`, mirrors `UserAccount`. Friend-code lookup by another user
  goes through the `lookup_friend_code(code)` SECURITY DEFINER function (never a direct SELECT —
  that would leak emails to strangers), which is rate-limited (20 lookups/hour per caller) via
  `friend_code_lookup_attempts` (`caller_id`, `attempted_at`) — RLS enabled, no policies, same
  zero-direct-client-access shape as `device_audit_log` below.
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
  count)` (SECURITY DEFINER). That function is the *only* place the 15-day cross-day cooldown
  logic exists — it excludes ids used in the last 15 days per `daily_quizzes` history, falls back
  to the unfiltered pool if too few remain, and atomically inserts-once/reads-after so every device
  gets the identical set. Territory Compare (a continuous survival queue, not a fixed N) asks for a
  larger pool (60 ids) and turns it into match order client-side with a `Random` seeded by the
  date, so the *shuffle* is deterministic and shared too, not just the item set.
- `year_guess_items`, `casualties_multiplier_items`, `flag_color_match_items`,
  `territory_compare_items`, `true_false_items`, `blind_map_battle_items` — the question banks,
  1:1 with the Kotlin data classes in `Data/Quiz/`, publicly readable, no client insert policy
  (content is team-maintained via SQL/dashboard). `QuizDataSource.kt`'s loaders take the specific
  id list to fetch (from `get_or_create_daily_quiz`), not "load everything." Images stay bundled
  as `commonMain/composeResources/files/images/...` (`imageAsset` is still a plain path relative to
  that root) — only the structured text/numeric data moved to Postgres, not the flag/battle PNGs.
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

## Fixed color palette

Each game gets its own Material3 `ColorScheme` built from one base color via `gameColorScheme()`
in `Core/Theme/Theme.kt` (`AppColors` object holds the palette). Don't introduce new colors outside
this palette without asking.

## Conventions

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
