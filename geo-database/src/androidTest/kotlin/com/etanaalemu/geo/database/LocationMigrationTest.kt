package com.etanaalemu.geo.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LocationDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_addsLocalizationColumns() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                """
                INSERT INTO countries (id, name, iso2, iso3, phoneCode, currency, capital)
                VALUES (1, 'Ethiopia', 'ET', 'ETH', '251', 'ETB', 'Addis Ababa')
                """.trimIndent(),
            )
            execSQL(
                """
                INSERT INTO states (id, name, countryId, stateCode)
                VALUES (1, 'Addis Ababa', 1, 'AA')
                """.trimIndent(),
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            LocationMigrations.MIGRATION_1_2,
        )

        db.query("PRAGMA table_info(countries)").use { cursor ->
            val columns = mutableListOf<String>()
            while (cursor.moveToNext()) {
                columns += cursor.getString(cursor.getColumnIndexOrThrow("name"))
            }
            check("nativeName" in columns)
            check("translationsJson" in columns)
        }
        db.query("SELECT nativeName, translationsJson FROM countries WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            check(cursor.getString(0).isEmpty())
            check(cursor.getString(1) == "{}")
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
