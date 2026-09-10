# Today in History

Android app (Kotlin, Jetpack Compose, Material3). Six daily history-trivia quiz games, each
playable once per day, resumable mid-round, with a real friends/profile layer backed by Supabase.

## Build

- Gradle 8.11.1 via the wrapper (`./gradlew` / `gradlew.bat`) — no separate Gradle install needed.
- `minSdk 26`, `targetSdk 36`, `compileSdk 36`, compose-bom 2024.09.00, AGP 8.10.0, Kotlin 2.2.20
  (Compose Compiler as its own Gradle plugin, not the old
  `composeOptions.kotlinCompilerExtensionVersion`).
- Kotlin is pinned to 2.2.20, not latest: Hilt/Dagger (any released version, as of this writing)
  can't parse the metadata format Kotlin 2.4.0 emits (google/dagger#5190), and 2.2.20 is what
  supabase-kt's pinned release was itself built with. Don't bump Kotlin past what Hilt supports
  without checking that issue first. Hilt is pinned to 2.58, not latest: 2.59 made the Hilt Gradle
  plugin require AGP 9.0, which this project isn't on.
- compileSdk/targetSdk 36 and AGP 8.10.0 exist specifically because `auth-kt` pulls in
  `androidx.browser` (its OAuth Custom Tabs dependency), which needs both.
- No test suite or CI exists yet.
- No JDK 17+ is installed system-wide on the dev machine used so far — builds use a JDK 21 found
  under the user profile (`C:\Users\<user>\.jdks\...`) via `$env:JAVA_HOME`.

## Supabase backend

Project "HistoryTrivia" (`xolybjypkkpeymsvfimr`). `Data/Remote/SupabaseClient.kt` installs
`Postgrest` and `Auth`; credentials come from `local.properties` (`SUPABASE_URL` /
`SUPABASE_ANON_KEY`, gitignored, exposed via `BuildConfig`).

**Auth**: every device signs in anonymously (`client.auth.signInAnonymously()` — no real
Google/Apple OAuth credentials exist) to get a real, verifiable `auth.uid()`. "Continue with
Google/Apple" (`AuthProvider.kt`'s `mockSignIn`) just attaches a display identity (provider, mock
email/name) to that one real session — see `AccountRepository.kt`. Guest mode never touches
Supabase at all; it stays fully device-local (SharedPreferences), same as before.

**Schema** (all in `public`, RLS enabled on every table):
- `profiles` — one row per `auth.uid()`, mirrors `UserAccount`. Friend-code lookup by another user
  goes through the `lookup_friend_code(code)` SECURITY DEFINER function (never a direct SELECT —
  that would leak emails to strangers).
- `friendships` — real friend requests/acceptances between real accounts (`FriendsRepository.kt`,
  `FriendsStore.kt`). Replaced the old hardcoded `FriendDirectory` demo directory and
  `demoScoreFor`'s synthetic scores entirely — deleted, not kept as fallback.
- `game_stats`, `score_history`, `quiz_progress` — one row per `(user_id, game_key[, date_key])`,
  owner-only RLS except `score_history`'s SELECT policy, which also allows an *accepted friend* to
  read it (that's what makes the Friends leaderboard real). A signed-in account's data lives here;
  a guest's stays in the original local SharedPreferences path — every one of these three stores
  branches on `AccountRepository.currentAccount()` being null.
- `daily_quizzes` — the fix for "same quiz everywhere on a given date": `(game_key, date_key) ->
  item_ids`, publicly readable, writable only through `get_or_create_daily_quiz(game_key, date,
  count)` (SECURITY DEFINER). That function is the *only* place the old 15-day cross-day cooldown
  logic exists now (it used to be `RecentlyUsedStore`, now deleted) — it excludes ids used in the
  last 15 days per `daily_quizzes` history, falls back to the unfiltered pool if too few remain,
  and atomically inserts-once/reads-after so every device gets the identical set. Territory
  Compare (a continuous survival queue, not a fixed N) asks for a larger pool (60 ids) and turns it
  into match order client-side with a `Random` seeded by the date, so the *shuffle* is
  deterministic and shared too, not just the item set.
- `year_guess_items`, `casualties_multiplier_items`, `flag_color_match_items`,
  `territory_compare_items`, `true_false_items`, `blind_map_battle_items` — the question banks,
  1:1 with the Kotlin data classes in `Data/Quiz/`, publicly readable, no client insert policy
  (content is team-maintained via SQL/dashboard). `QuizDataSource.kt`'s loaders take the specific
  id list to fetch (from `get_or_create_daily_quiz`), not "load everything." Images stay bundled
  local APK assets (`imageAsset` is still a plain asset-relative path) — only the ~3,080 rows of
  structured text/numeric data moved to Postgres, not the ~17 MB of flag/battle PNGs.
- `device_audit_log` — sign-in security audit trail (IP, user agent, event, timestamp). **No RLS
  policies at all** — zero client access, not even the account's own row. Only reachable through
  `log_device_event(event)` (SECURITY DEFINER), which reads the caller's IP from PostgREST's
  `request.headers` so the client can never spoof it. Disclosed in `SettingsScreen.kt`'s privacy
  policy text.
- `get_final_scores(game_key, date)` / `get_question_guesses(game_key, date, question_id)` — both
  SECURITY DEFINER, both return only anonymized aggregate values (never which user), because
  `score_history`/`quiz_progress`'s RLS is otherwise owner/friend-only by design. This is what
  `DailyDistribution.kt` (rewritten — no longer a seeded-random generator) calls for the
  results-screen rank display and per-question guess-distribution axes; "total players today" is
  simply the length of `get_final_scores`' real result, not a synthetic ramp.

**When adding a new query that needs "everyone's data" for some anonymized aggregate**: don't
widen a table's RLS policy to make it work — add a narrow SECURITY DEFINER function that returns
only the aggregate, following the two examples above.

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
- Guest mode (no account) is the only place "mock-now/local-only" still applies by design — a
  deliberate trade-off (no friends, no cross-device sync), not a placeholder for something not yet
  built. Everywhere else, prefer the real Supabase-backed path; don't reintroduce a synthetic/local
  fallback for a signed-in account.
- This machine has no `git` on PATH by default; it's at `C:\Program Files\Git\bin\git.exe`.
