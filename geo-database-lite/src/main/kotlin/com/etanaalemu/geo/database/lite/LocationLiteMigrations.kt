package com.etanaalemu.geo.database.lite

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.etanaalemu.geo.database.common.migration.Dr5hnRegionMigrationSql

internal object LocationLiteMigrations {
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

    /**
     * Regions / subregions reference tables, dr5hn-style country & state metadata.
     * Existing rows get empty / zero defaults; reinstall or regenerate the lite asset for full data.
     */
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
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
        }
    }
}
