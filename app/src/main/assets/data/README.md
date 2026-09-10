# Quiz data

**No longer read at runtime.** These JSON files were the original source of truth, one flat array
per game; they were bulk-loaded once into Supabase's `<game_key>_items` tables (see `CLAUDE.md`),
and `Data/Quiz/QuizDataSource.kt`'s loaders now fetch from Postgres by id instead of parsing these
files. They're kept here as the historical seed record / a reference for the schema conventions
below — editing them does nothing to the live app. To change quiz content now, edit the Supabase
table directly (or re-run a bulk load from an edited copy of these files).

| Folder | Kotlin class | Postgres table |
|---|---|---|
| `year_guess/` | `HistoricalEvent` | `year_guess_items` |
| `casualties_multiplier/` | `BattleCasualty` | `casualties_multiplier_items` |
| `flag_color_match/` | `FlagColor` | `flag_color_match_items` |
| `territory_compare/` | `TerritoryCountry` | `territory_compare_items` |
| `true_false/` | `TrueFalseStatement` | `true_false_items` |
| `blind_map_battle/` | `BattleLocation` | `blind_map_battle_items` |

## Conventions

- **Years are signed integers**, not strings: negative = BCE, positive = AD (e.g. `-31` = 31 BCE).
  Format for display with `ScoringUtils.formatEraYear`, never hand-roll "BCE"/"AD" suffixing.
- **IDs are globally unique within their table** and are the join key `get_or_create_daily_quiz`
  and `quiz_progress` use to track what's been assigned/seen — don't reuse an id across an edit, or
  you'll silently corrupt another entry's cooldown/resume history.
- **`imageAsset`** (where present) is a path relative to `assets/`, e.g. `"images/battles/battle_001.png"`,
  resolved via `AssetManager` at runtime — not a `res/drawable` resource.
- **`hint`** (Annalist/Herald only) is the war/conflict this event or battle belongs to, shown
  behind the "watch an ad" hint dialog. Keep it short (a war/conflict name, not a sentence).
- **Adding a new field**: update the Kotlin data class (`@SerialName` for the snake_case column),
  the Postgres table (`alter table ... add column`), and backfill every existing row — don't leave
  a required field missing on old rows.
- **Bulk-generated content must be genuinely distinct facts.** A prior 1700-statement batch turned
  out to be ~90 real facts reworded into many near-duplicates via templated lead-in phrases — this
  defeats the 15-day cooldown in `get_or_create_daily_quiz`, since two "different" ids can carry the
  same fact reworded. Dedupe by underlying fact before adding, not just by exact string match.
