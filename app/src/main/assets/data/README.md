# Quiz data

One folder per game, one flat JSON array per folder, loaded by the matching function in
`Data/Quiz/QuizDataSource.kt`. Loaders use non-optional `org.json` calls (`getString`, `getInt`,
`getLong`) for every required field — a missing key throws at load time, not compile time — so
every row in a file must have every field the corresponding Kotlin data class declares as non-null.

| Folder | Kotlin class | Loader |
|---|---|---|
| `year_guess/` | `HistoricalEvent` | `loadHistoricalEvents` |
| `casualties_multiplier/` | `BattleCasualty` | `loadBattleCasualties` |
| `flag_color_match/` | `FlagColor` | `loadFlagColors` |
| `territory_compare/` | `TerritoryCountry` | `loadTerritories` |
| `true_false/` | `TrueFalseStatement` | `loadTrueFalseStatements` |
| `blind_map_battle/` | `BattleLocation` | `loadBattleLocations` |

## Conventions

- **Years are signed integers**, not strings: negative = BCE, positive = AD (e.g. `-31` = 31 BCE).
  Format for display with `ScoringUtils.formatEraYear`, never hand-roll "BCE"/"AD" suffixing.
- **IDs are globally unique within their file** and are the join key `RecentlyUsedStore` and
  `QuizProgressStore` use to track what a player has already seen — don't reuse an id across an
  edit, or you'll silently corrupt another entry's cooldown/resume history.
- **`imageAsset`** (where present) is a path relative to `assets/`, e.g. `"images/battles/battle_001.png"`,
  resolved via `AssetManager` at runtime — not a `res/drawable` resource.
- **`hint`** (Annalist/Herald only) is the war/conflict this event or battle belongs to, shown
  behind the "watch an ad" hint dialog. Keep it short (a war/conflict name, not a sentence).
- **Adding a new field**: update the Kotlin data class, the loader in `QuizDataSource.kt`, and
  backfill every existing row in the JSON file — don't leave a required field missing on old rows.
- **Bulk-generated content must be genuinely distinct facts.** A prior 1700-statement batch turned
  out to be ~90 real facts reworded into many near-duplicates via templated lead-in phrases — this
  defeats `RecentlyUsedStore`'s 15-day cooldown, since two "different" ids can carry the same fact
  reworded. Dedupe by underlying fact before adding, not just by exact string match.
