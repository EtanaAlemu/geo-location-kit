package com.etanaalemu.geo.dbbuilder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.sql.DriverManager

class LocationDatabaseBuilderTest {
    @Test
    fun build_insertsFixtureRowsAndSupportsPrefixSearch() {
        val fixturesDir = File(
            javaClass.classLoader.getResource("fixtures")!!.toURI(),
        )
        val outputDb = File.createTempFile("location-test", ".db")
        outputDb.deleteOnExit()

        val result = LocationDatabaseBuilder().build(
            LocationDatabaseBuilder.Config(
                dataDir = fixturesDir,
                outputDb = outputDb,
                skipDownload = true,
                useNestedCombinedJson = false,
            ),
        )

        assertEquals(1, result.countryCount)
        assertEquals(1, result.stateCount)
        assertEquals(2, result.cityCount)
        assertEquals(0, result.postcodeCount)

        DriverManager.getConnection("jdbc:sqlite:${outputDb.absolutePath}").use { connection ->
            connection.createStatement().use { statement ->
                val countryCount = statement.executeQuery("SELECT COUNT(*) FROM countries").use {
                    it.next()
                    it.getInt(1)
                }
                assertEquals(1, countryCount)

                val prefixMatches = statement.executeQuery(
                    """
                    SELECT COUNT(*) FROM cities
                    WHERE stateId = 10 AND name LIKE 'A%'
                    """.trimIndent(),
                ).use {
                    it.next()
                    it.getInt(1)
                }
                assertEquals(1, prefixMatches)

                val indexExists = statement.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'index' AND name = 'index_cities_stateId_name'",
                ).use { rs ->
                    rs.next()
                    rs.getString(1)
                }
                assertEquals("index_cities_stateId_name", indexExists)
            }
        }

        assertTrue(outputDb.length() > 0)
    }
}
