package com.etanaalemu.geo.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeoDatabaseInstrumentedTest {
    @Test
    fun prebuiltDatabase_opensAndSupportsPrefixCitySearch() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = GeoLocationKit.initialize(context)

        val countries = repository.getCountries().first()
        assertTrue("Expected countries in prebuilt DB", countries.size >= 200)

        val countryCount = GeoDatabaseClient.getInstance(context)
            .locationDao()
            .getCountryCount()
        assertTrue(countryCount >= 200)

        val sampleCountry = countries.first { it.iso2 == "US" }
        val states = repository.getStatesByCountry(sampleCountry.id).first()
        assertTrue(states.isNotEmpty())

        val sampleState = states.first()
        val citiesWithPrefix = repository.searchCitiesInState(sampleState.id, "A").first()
        assertTrue(citiesWithPrefix.size <= 50)
        assertTrue(citiesWithPrefix.all { it.name.startsWith("A", ignoreCase = true) })
    }

    @Test
    fun citySearch_ftsFindsByTokenPrefix() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = GeoLocationKit.initialize(context)
        val us = repository.getCountryByIso2("US")!!
        val illinois = repository.getStatesByCountry(us.id).first().first { it.name == "Illinois" }
        val cities = repository.searchCitiesInState(illinois.id, "spring").first()
        assertTrue(cities.any { it.name.equals("Springfield", ignoreCase = true) })
    }

    @Test
    fun locale_amharic_usesNativeCountryName() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val kit = GeoLocationKit.initialize(context, GeoLanguageConfig.fixed("am"))

        val ethiopia = kit.getCountries().first().first { it.iso2 == "ET" }
        assertTrue(ethiopia.name.contains("ኢትዮ"))
    }

    @Test
    fun getCountryByIso2_returnsCountry() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val kit = GeoLocationKit.initialize(context)

        val ethiopia = kit.getCountryByIso2("et")
        assertEquals("ET", ethiopia?.iso2)
        assertEquals("Ethiopia", ethiopia?.name)
    }

    @Test
    fun findCountriesByPhone_sharedCode_returnsMultiple() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val kit = GeoLocationKit.initialize(context)

        val matches = kit.findCountriesByPhone("+1")
        assertTrue(matches.size > 1)
        val us = kit.findCountryByPhone("1", iso2Hint = "US")
        assertEquals("US", us?.iso2)
    }
}
