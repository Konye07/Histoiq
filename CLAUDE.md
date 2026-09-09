# Today in History

Android app (Kotlin, Jetpack Compose, Material3). Six daily history-trivia quiz games, each
playable once per day, resumable mid-round, with a friends/profile layer on top.

## Build

- Gradle 8.7 via the wrapper (`./gradlew` / `gradlew.bat`) — no separate Gradle install needed.
- `minSdk 26`, `targetSdk 35`, compose-bom 2024.09.00, AGP 8.6.1, Kotlin 2.2.20 (Compose Compiler as
  its own Gradle plugin, not the old `composeOptions.kotlinCompilerExtensionVersion`).
- Kotlin is pinned to 2.2.20, not latest: Hilt/Dagger (any released version, as of this writing)
  can't parse the metadata format Kotlin 2.4.0 emits (google/dagger#5190), and 2.2.20 is what
  supabase-kt's pinned release was itself built with. Don't bump Kotlin past what Hilt supports
  without checking that issue first.
- No test suite or CI exists yet.
- Supabase connection: `Data/Remote/SupabaseClient.kt`, credentials in `local.properties`
  (`SUPABASE_URL` / `SUPABASE_ANON_KEY`, gitignored, exposed via `BuildConfig`) — connection only,
  nothing reads/writes through it yet. Only `postgrest-kt` is installed; `auth-kt` is deliberately
  left out (see the comment in `app/build.gradle.kts`) until auth actually migrates off the mock.

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
and color — screens must reference `GameKeys.X`, never re-declare the key as a local string literal.

## Fixed color palette

Each game gets its own Material3 `ColorScheme` built from one base color via `gameColorScheme()`
in `Core/Theme/Theme.kt` (`AppColors` object holds the palette). Don't introduce new colors outside
this palette without asking.

## Data pipeline

All quiz content lives as JSON under `app/src/main/assets/data/<game>/`, loaded via
`Data/Quiz/QuizDataSource.kt` (`org.json`, no Room/SQL). Each game's Kotlin data class must have a
field for every JSON key the loader reads — the loaders use non-optional `get*` calls, so a missing
key throws at load time, not compile time. When adding a field, update: the data class, the loader,
and every existing JSON row (backfill, don't leave nulls unless the field is genuinely optional).

Selection/repeat rules:
- `QuizProgressStore` — same-CET-day resume/lock (a quiz already played today can't restart).
- `RecentlyUsedStore` — a 15-day cross-day cooldown so the same item doesn't reappear too soon.
  Every game's "no stored progress" branch must route its pool through `RecentlyUsedStore.pickDaily`
  (fixed-N games) or `.eligiblePool` (Cosmographer's continuous queue) — never a bare
  `pool.shuffled().take(n)`.
- `DailyDistribution` — synthetic "how did today's other players do" data (deterministically
  seeded by date, same idea as `Data/Social/DemoScores.kt` for Friends) used for the results
  screen's rank display and per-question guess-distribution axes. Mock until a real backend exists.

## Conventions

- No comments unless the *why* is genuinely non-obvious (a workaround, a subtle invariant). Never
  comments that just restate what a well-named identifier already says.
- Historical images/flags: only ever sourced from Wikimedia Commons with a verified public-domain
  or CC license, downloaded after explicit permission each time. Never fabricate or scrape from
  sources with unclear licensing.
- True/false statements and other bulk trivia content must be genuinely distinct facts — an earlier
  batch turned out to be ~90 real facts reworded into 1700 near-duplicates via templated lead-in
  phrases, which defeats the 15-day repeat cooldown. Dedupe by underlying fact, not by exact string.
- Mock-now/real-later is the established pattern for anything needing a backend that doesn't exist
  yet (auth, friends, daily-distribution rankings) — build the real UI/logic against a clearly
  isolated mock data source, not a half-real half-fake shortcut.
- This machine has no `git` on PATH by default; it's at `C:\Program Files\Git\bin\git.exe`.
