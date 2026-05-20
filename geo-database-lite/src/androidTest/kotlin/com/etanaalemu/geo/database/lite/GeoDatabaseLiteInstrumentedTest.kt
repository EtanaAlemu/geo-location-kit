package com.etanaalemu.geo.database.lite

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
class GeoDatabaseLiteInstrumentedTest {
    @Test
    fun prebuiltLiteDatabase_hasCountriesAndStatesWithoutCities() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val kit = GeoLocationKitLite.initialize(context)

        val countries = kit.getCountries().first()
        assertTrue(countries.size >= 200)

        val ethiopia = kit.getCountryByIso2("ET")
        assertEquals("ET", ethiopia?.iso2)

        val states = kit.getStatesByCountry(ethiopia!!.id).first()
        assertTrue(states.isNotEmpty())
    }

    @Test
    fun locale_amharic_usesNativeCountryName() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val kit = GeoLocationKitLite.initialize(context, GeoLanguageConfig.fixed("am"))

        val ethiopia = kit.getCountries().first().first { it.iso2 == "ET" }
        assertTrue(ethiopia.name.contains("ኢትዮ"))
    }
}
