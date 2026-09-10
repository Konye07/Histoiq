# Today in History

An Android history-trivia app built with Kotlin and Jetpack Compose. Six daily quiz games test
different kinds of historical knowledge, each playable once per day and resumable mid-round.
Accounts, friends, stats, and the daily quizzes themselves are backed by Supabase, so every device
sees the exact same questions on the same date, and real friends can see each other's scores.

## The six games

| Role name | Mechanic |
|---|---|
| Annalist | Guess the year of a historical event or battle |
| Herald | Guess a battle's casualty count (scored as a multiplier — closer is better) |
| Officer-of-Arms | Recolor a historical flag's dominant color to match the original |
| Cosmographer | Pick the larger of two countries by peak territorial size, sudden-death |
| Chronicler | True/false historical statements, sudden-death |
| Chancellor | Tap a battle's location on a blank world map |

## Building

Requires JDK 17+ and the Android SDK. The Gradle wrapper is checked in, so no separate Gradle
install is needed:

```
./gradlew assembleDebug
```

(`gradlew.bat` on Windows.) Open the project in Android Studio for the usual run/debug workflow.
You'll also need a `local.properties` with `SUPABASE_URL` and `SUPABASE_ANON_KEY` set (see
`app/build.gradle.kts`) — without them the app builds but can't reach the backend.

## Project layout

- `app/src/main/java/com/yourorg/app/` — Kotlin source.
  - `Features/Play/` — one package per game, plus `Features/Play/Shared/` for cross-game UI
    (results header, guess-distribution axis, hint/ad dialog).
  - `Data/Quiz/` — data classes, Supabase-backed loaders (`QuizDataSource.kt`), scoring
    (`ScoringUtils.kt`), the shared daily-quiz assignment (`DailyQuizRepository.kt`), and per-account
    progress/stats/history/leaderboard stores.
  - `Data/Account/` — anonymous-auth-backed accounts (`AccountRepository.kt`).
  - `Data/Social/` — real friend requests/leaderboard (`FriendsRepository.kt`, `FriendsStore.kt`).
  - `Data/Remote/` — the single Supabase client (`SupabaseClient.kt`).
  - `Core/Theme/` — the fixed color palette and per-game `ColorScheme` generation.
- `app/src/main/assets/data/` — the question banks used to *live* here as JSON; they're in Supabase
  now (see `CLAUDE.md`), but the README there still documents each game's field-level schema.
- `app/src/main/assets/images/` — flag/battle/event artwork (sourced from Wikimedia Commons under
  verified public-domain or CC licenses) plus the world map used by Chancellor; stays bundled in
  the APK rather than moving to Supabase Storage.

See `CLAUDE.md` for the full Supabase schema/architecture and build-toolchain notes.

## Status

No automated test suite or CI exists yet. Guest mode (no account) is the only remaining
local-only/mocked path, by design — everything else (auth, friends, daily quizzes, stats, score
history, leaderboards) is real and backed by Supabase.
