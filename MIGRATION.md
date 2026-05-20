# Database migrations

`location.db` is a Room database (currently **version 4**).

## v1 → v2

Adds localization columns:

| Table | Columns |
|-------|---------|
| `countries` | `nativeName`, `translationsJson` |
| `states` | `nativeName` |

`LocationMigrations.MIGRATION_1_2` runs `ALTER TABLE … ADD COLUMN` with safe defaults (`''` / `'{}'`).

### After upgrading from v1

- **Schema** is migrated in place; existing country/state/city rows are kept.
- **Translation data** for the new columns is only present when the app ships a **v2 pre-built asset**. If you released v1 without those columns, migrated rows keep empty defaults until the user clears app data or you ship a new asset and bump the Room version with a data refresh strategy.

## v2 → v3

Adds **FTS4** external-content virtual table `cities_fts` on `cities.name` (with `prefix=` for prefix queries) plus Room sync triggers, then `INSERT INTO cities_fts(cities_fts) VALUES('rebuild')` to populate from existing city rows.

## v3 → v4

Adds **regions** and **subregions** (with seeded UN-style rows), extended **countries** / **states** / **cities** columns (dr5hn metadata: region ids, currency display, emoji, coordinates, state `timezone` / `iso3166_2`, city `timezone`), **country_timezones**, and **postcodes**.

Existing rows receive empty or zero defaults; **postcodes and enriched fields are not backfilled** from nested JSON by SQL alone. For full data, ship a new **v4** pre-built `location.db` (regenerate with `generateLocationDatabase`) or clear local storage so Room recopies the asset.

## `location-lite.db` (Room **version 3**)

Lite adds the same **regions** / **subregions** tables and extended **countries** / **states** columns as the full DB, but **no** `cities`, `cities_fts`, `country_timezones`, or `postcodes`.

| Migration | Summary |
|-----------|---------|
| v1 → v2 | Same i18n columns as full DB. |
| v2 → v3 | Regions, subregions, extended country/state columns (defaults on upgrade). |

## Fresh installs

Room copies the pre-built asset (already at **v4** for `geo-database` and **v3** for `geo-database-lite` after you regenerate). No migration runs.

| Module | Asset |
|--------|-------|
| `geo-database` | `location.db` |
| `geo-database-lite` | `location-lite.db` (countries + states + regions) |

## Regenerating the asset

After a Room **version bump**:

```bash
./gradlew generateLocationDatabase
./gradlew generateLocationLiteDatabase
```

Rebuild so KSP exports the new schema under `geo-database/schemas/…/` (and `geo-database-lite/schemas/…/`) and the builder writes the matching `identity_hash` into the asset.

Optional: `-Pdr5hnReleaseTag=v3.2-export.2` and `-Pdr5hnSkipDownload` (see `geo-db-builder` Gradle tasks).

## Unknown versions

`fallbackToDestructiveMigration(dropAllTables = true)` remains as a last resort when no migration path exists (e.g. a skipped major version). Library consumers should pin releases and read this file when upgrading.
