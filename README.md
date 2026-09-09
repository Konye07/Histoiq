# Today in History

An Android history-trivia app built with Kotlin and Jetpack Compose. Six daily quiz games test
different kinds of historical knowledge, each playable once per day and resumable mid-round.

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

## Project layout

- `app/src/main/java/com/yourorg/app/` — Kotlin source.
  - `Features/Play/` — one package per game, plus `Features/Play/Shared/` for cross-game UI
    (results header, guess-distribution axis, hint/ad dialog).
  - `Data/Quiz/` — data classes, JSON loaders (`QuizDataSource.kt`), scoring (`ScoringUtils.kt`),
    and the daily-selection/cooldown logic (`QuizProgressStore`, `RecentlyUsedStore`).
  - `Data/Account/`, `Data/Social/` — auth and friends, currently mocked pending a real backend.
  - `Core/Theme/` — the fixed color palette and per-game `ColorScheme` generation.
- `app/src/main/assets/data/` — one JSON file per game; see the README there for the schema
  conventions.
- `app/src/main/assets/images/` — flag/battle/event artwork (sourced from Wikimedia Commons under
  verified public-domain or CC licenses) plus the world map used by Chancellor.

See `CLAUDE.md` for more detail on conventions used throughout the codebase.

## Status

No automated test suite or CI exists yet. Auth and the friends/leaderboard feature are mocked
(no real backend); a Supabase migration is the intended next step for both.
