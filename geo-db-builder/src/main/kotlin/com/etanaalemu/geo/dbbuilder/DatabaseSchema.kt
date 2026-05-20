package com.etanaalemu.geo.dbbuilder

internal object DatabaseSchema {
    const val VERSION = 4

    private val CITIES_FTS_VIRTUAL = """
        CREATE VIRTUAL TABLE IF NOT EXISTS `cities_fts` USING FTS4(`name` TEXT NOT NULL, content=`cities`, prefix=`2,3,4,5,6,7,8,9,10`)
        """.trimIndent()

    private val CITIES_FTS_TRIGGERS = listOf(
        "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_BEFORE_UPDATE BEFORE UPDATE ON `cities` BEGIN DELETE FROM `cities_fts` WHERE `docid`=OLD.`rowid`; END",
        "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_BEFORE_DELETE BEFORE DELETE ON `cities` BEGIN DELETE FROM `cities_fts` WHERE `docid`=OLD.`rowid`; END",
        "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_AFTER_UPDATE AFTER UPDATE ON `cities` BEGIN INSERT INTO `cities_fts`(`docid`, `name`) VALUES (NEW.`rowid`, NEW.`name`); END",
        "CREATE TRIGGER IF NOT EXISTS room_fts_content_sync_cities_fts_AFTER_INSERT AFTER INSERT ON `cities` BEGIN INSERT INTO `cities_fts`(`docid`, `name`) VALUES (NEW.`rowid`, NEW.`name`); END",
    )

    private val REGIONS_DDL = """
        CREATE TABLE IF NOT EXISTS regions (
            id INTEGER NOT NULL PRIMARY KEY,
            name TEXT NOT NULL
        )
        """.trimIndent()

    private val SUBREGIONS_DDL = """
        CREATE TABLE IF NOT EXISTS subregions (
            id INTEGER NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            regionId INTEGER NOT NULL,
            FOREIGN KEY(regionId) REFERENCES regions(id) ON DELETE CASCADE
        )
        """.trimIndent()

    private val COUNTRIES_DDL = """
        CREATE TABLE IF NOT EXISTS countries (
            id INTEGER NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            iso2 TEXT NOT NULL,
            iso3 TEXT NOT NULL,
            phoneCode TEXT NOT NULL,
            currency TEXT NOT NULL,
            capital TEXT NOT NULL,
            nativeName TEXT NOT NULL,
            translationsJson TEXT NOT NULL,
            regionId INTEGER NOT NULL,
            subregionId INTEGER NOT NULL,
            currencyName TEXT NOT NULL,
            currencySymbol TEXT NOT NULL,
            emoji TEXT NOT NULL,
            emojiU TEXT NOT NULL,
            nationality TEXT NOT NULL,
            numericCode TEXT NOT NULL,
            population INTEGER NOT NULL,
            gdp TEXT NOT NULL,
            tld TEXT NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL
        )
        """.trimIndent()

    private val STATES_DDL = """
        CREATE TABLE IF NOT EXISTS states (
            id INTEGER NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            countryId INTEGER NOT NULL,
            stateCode TEXT NOT NULL,
            nativeName TEXT NOT NULL,
            timezone TEXT NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            type TEXT NOT NULL,
            iso3166_2 TEXT NOT NULL,
            FOREIGN KEY(countryId) REFERENCES countries(id) ON DELETE CASCADE
        )
        """.trimIndent()

    private val CITIES_DDL = """
        CREATE TABLE IF NOT EXISTS cities (
            id INTEGER NOT NULL PRIMARY KEY,
            name TEXT NOT NULL,
            stateId INTEGER NOT NULL,
            countryId INTEGER NOT NULL,
            latitude REAL NOT NULL,
            longitude REAL NOT NULL,
            timezone TEXT NOT NULL,
            FOREIGN KEY(stateId) REFERENCES states(id) ON DELETE CASCADE,
            FOREIGN KEY(countryId) REFERENCES countries(id) ON DELETE CASCADE
        )
        """.trimIndent()

    private val COUNTRY_TIMEZONES_DDL = """
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
        """.trimIndent()

    private val POSTCODES_DDL = """
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
        """.trimIndent()

    private val ROOM_MASTER_DDL = """
        CREATE TABLE IF NOT EXISTS room_master_table (
            id INTEGER PRIMARY KEY,
            identity_hash TEXT
        )
        """.trimIndent()

    private val COUNTRY_INDEXES = listOf(
        "CREATE UNIQUE INDEX IF NOT EXISTS index_countries_iso2 ON countries (iso2)",
        "CREATE INDEX IF NOT EXISTS index_countries_phoneCode ON countries (phoneCode)",
        "CREATE INDEX IF NOT EXISTS index_countries_regionId ON countries (regionId)",
        "CREATE INDEX IF NOT EXISTS index_countries_subregionId ON countries (subregionId)",
    )

    val LITE_DDL = listOf(
        REGIONS_DDL,
        SUBREGIONS_DDL,
        "CREATE INDEX IF NOT EXISTS index_subregions_regionId ON subregions (regionId)",
        COUNTRIES_DDL,
    ) + COUNTRY_INDEXES + listOf(
        STATES_DDL,
        "CREATE INDEX IF NOT EXISTS index_states_countryId ON states (countryId)",
        ROOM_MASTER_DDL,
    )

    val FULL_DDL = listOf(
        REGIONS_DDL,
        SUBREGIONS_DDL,
        "CREATE INDEX IF NOT EXISTS index_subregions_regionId ON subregions (regionId)",
        COUNTRIES_DDL,
    ) + COUNTRY_INDEXES + listOf(
        STATES_DDL,
        "CREATE INDEX IF NOT EXISTS index_states_countryId ON states (countryId)",
        CITIES_DDL,
        "CREATE INDEX IF NOT EXISTS index_cities_stateId ON cities (stateId)",
        "CREATE INDEX IF NOT EXISTS index_cities_countryId ON cities (countryId)",
        "CREATE INDEX IF NOT EXISTS index_cities_stateId_name ON cities (stateId, name)",
        COUNTRY_TIMEZONES_DDL,
        "CREATE INDEX IF NOT EXISTS index_country_timezones_countryId ON country_timezones (countryId)",
        POSTCODES_DDL,
        "CREATE INDEX IF NOT EXISTS index_postcodes_countryId ON postcodes (countryId)",
        "CREATE INDEX IF NOT EXISTS index_postcodes_countryId_code ON postcodes (countryId, code)",
        "CREATE INDEX IF NOT EXISTS index_postcodes_stateId ON postcodes (stateId)",
        "CREATE INDEX IF NOT EXISTS index_postcodes_cityId ON postcodes (cityId)",
        CITIES_FTS_VIRTUAL,
    ) + CITIES_FTS_TRIGGERS + listOf(
        ROOM_MASTER_DDL,
    )

    /** @deprecated Use [FULL_DDL] */
    val DDL = FULL_DDL

    fun ddl(includeCities: Boolean): List<String> = if (includeCities) FULL_DDL else LITE_DDL

    const val ROOM_IDENTITY_HASH_PLACEHOLDER =
        "placeholder_will_be_replaced_on_first_room_compile"
}
