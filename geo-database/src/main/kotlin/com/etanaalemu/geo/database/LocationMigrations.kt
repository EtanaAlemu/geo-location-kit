package com.etanaalemu.geo.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.etanaalemu.geo.database.common.migration.Dr5hnRegionMigrationSql

internal object LocationMigrations {
    /**
     * Adds i18n columns introduced in schema v2:
     * - `countries.nativeName`, `countries.translationsJson`
     * - `states.nativeName`
     *
     * Existing rows receive empty defaults; reinstall or regenerate the asset for full translation data.
     */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE countries ADD COLUMN nativeName TEXT NOT NULL DEFAULT ''",
            )
            db.execSQL(
                "ALTER TABLE countries ADD COLUMN translationsJson TEXT NOT NULL DEFAULT '{}'",
            )
            db.execSQL(
                "ALTER TABLE states ADD COLUMN nativeName TEXT NOT NULL DEFAULT ''",
            )
        }
    }

    /** FTS4 external-content index on city names (prefix search). */
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS `cities_fts` USING FTS4(`name` TEXT NOT NULL, content=`cities`, prefix=`2,3,4,5,6,7,8,9,10`)
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_BEFORE_UPDATE BEFORE UPDATE ON `cities` BEGIN DELETE FROM `cities_fts` WHERE `docid`=OLD.`rowid`; END",
            )
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_BEFORE_DELETE BEFORE DELETE ON `cities` BEGIN DELETE FROM `cities_fts` WHERE `docid`=OLD.`rowid`; END",
            )
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_AFTER_UPDATE AFTER UPDATE ON `cities` BEGIN INSERT INTO `cities_fts`(`docid`, `name`) VALUES (NEW.`rowid`, NEW.`name`); END",
            )
            db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_AFTER_INSERT AFTER INSERT ON `cities` BEGIN INSERT INTO `cities_fts`(`docid`, `name`) VALUES (NEW.`rowid`, NEW.`name`); END",
            )
            db.execSQL("INSERT INTO `cities_fts`(`cities_fts`) VALUES('rebuild')")
        }
    }

    /**
     * Regions / subregions, extended country & state columns, city timezones, country timezones, postcodes.
     * Existing rows get empty / zero defaults; reinstall or regenerate the bundled DB for full dr5hn data.
     */
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS regions (
                    id INTEGER NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS subregions (
                    id INTEGER NOT NULL PRIMARY KEY,
                    name TEXT NOT NULL,
                    regionId INTEGER NOT NULL,
                    FOREIGN KEY(regionId) REFERENCES regions(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_subregions_regionId ON subregions (regionId)",
            )
            Dr5hnRegionMigrationSql.seedRegionsAndSubregions(db)

            db.execSQL("ALTER TABLE countries ADD COLUMN regionId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE countries ADD COLUMN subregionId INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE countries ADD COLUMN currencyName TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN currencySymbol TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN emoji TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN emojiU TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN nationality TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN numericCode TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN population INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE countries ADD COLUMN gdp TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN tld TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE countries ADD COLUMN latitude REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE countries ADD COLUMN longitude REAL NOT NULL DEFAULT 0")
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_countries_regionId ON countries (regionId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_countries_subregionId ON countries (subregionId)",
            )

            db.execSQL("ALTER TABLE states ADD COLUMN timezone TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE states ADD COLUMN latitude REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE states ADD COLUMN longitude REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE states ADD COLUMN type TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE states ADD COLUMN iso3166_2 TEXT NOT NULL DEFAULT ''")

            db.execSQL("ALTER TABLE cities ADD COLUMN timezone TEXT NOT NULL DEFAULT ''")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS country_timezones (
                    countryId INTEGER NOT NULL,
                    zoneName TEXT NOT NULL,
                    gmtOffset INTEGER NOT NULL,
                    gmtOffsetName TEXT NOT NULL,
                    abbreviation TEXT NOT NULL,
                    tzName TEXT NOT NULL,
                    PRIMARY KEY(countryId, zoneName),
                    FOREIGN KEY(countryId) REFERENCES countries(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_country_timezones_countryId ON country_timezones (countryId)",
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS postcodes (
                    id INTEGER NOT NULL PRIMARY KEY,
                    code TEXT NOT NULL,
                    countryId INTEGER NOT NULL,
                    countryCode TEXT NOT NULL,
                    stateId INTEGER NOT NULL,
                    stateCode TEXT NOT NULL,
                    cityId INTEGER,
                    localityName TEXT NOT NULL,
                    type TEXT NOT NULL,
                    latitude REAL,
                    longitude REAL,
                    source TEXT NOT NULL,
                    wikiDataId TEXT,
                    FOREIGN KEY(countryId) REFERENCES countries(id) ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_postcodes_countryId ON postcodes (countryId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_postcodes_countryId_code ON postcodes (countryId, code)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_postcodes_stateId ON postcodes (stateId)",
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_postcodes_cityId ON postcodes (cityId)",
            )
        }
    }
}
