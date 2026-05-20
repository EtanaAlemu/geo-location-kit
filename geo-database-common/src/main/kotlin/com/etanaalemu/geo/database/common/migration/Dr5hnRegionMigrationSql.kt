package com.etanaalemu.geo.database.common.migration

import androidx.sqlite.db.SupportSQLiteDatabase

/** Static dr5hn region rows for migrations and the offline builder. */
object Dr5hnRegionMigrationSql {
    fun seedRegionsAndSubregions(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (1, 'Africa')")
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (2, 'Americas')")
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (3, 'Asia')")
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (4, 'Europe')")
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (5, 'Oceania')")
        db.execSQL("INSERT OR REPLACE INTO regions (id, name) VALUES (6, 'Polar')")

        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (1, 'Northern Africa', 1)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (2, 'Middle Africa', 1)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (3, 'Western Africa', 1)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (4, 'Eastern Africa', 1)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (5, 'Southern Africa', 1)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (6, 'Northern America', 2)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (7, 'Caribbean', 2)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (8, 'South America', 2)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (9, 'Central America', 2)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (10, 'Central Asia', 3)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (11, 'Western Asia', 3)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (12, 'Eastern Asia', 3)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (13, 'South-Eastern Asia', 3)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (14, 'Southern Asia', 3)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (15, 'Eastern Europe', 4)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (16, 'Southern Europe', 4)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (17, 'Western Europe', 4)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (18, 'Northern Europe', 4)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (19, 'Australia and New Zealand', 5)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (20, 'Melanesia', 5)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (21, 'Micronesia', 5)",
        )
        db.execSQL(
            "INSERT OR REPLACE INTO subregions (id, name, regionId) VALUES (22, 'Polynesia', 5)",
        )
    }
}
