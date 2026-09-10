# Seed data (not shipped in the app)

Historical record of how the Supabase question-bank tables were originally populated — **not
read by the app at runtime** (nothing here is bundled into the APK). See `data/README.md` for
the per-game schema conventions, and `CLAUDE.md` at the repo root for the live Postgres schema.

- `data/<game>/*.json` — the flat arrays bulk-loaded once into each `<game_key>_items` table.
- `*.txt` — raw source dumps one generation further back, used to originally generate the JSON
  above for `true_false`, `blind_map_battle`, and part of `year_guess`.

To change quiz content now, edit the Supabase tables directly. These files are useful only if you
ever need to re-seed a fresh Supabase project from scratch.
