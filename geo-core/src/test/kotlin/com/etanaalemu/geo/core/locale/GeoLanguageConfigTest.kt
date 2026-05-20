package com.etanaalemu.geo.core.locale

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoLanguageConfigTest {
    @Test
    fun english_isDefaultFixedEn() {
        val config = GeoLanguageConfig.English
        assertFalse(config.useAppLanguage)
        assertEquals("en", config.fixedLocale.languageTag)
    }

    @Test
    fun appLanguage_usesAppFlag() {
        assertTrue(GeoLanguageConfig.AppLanguage.useAppLanguage)
    }

    @Test
    fun fixed_setsTag() {
        val config = GeoLanguageConfig.fixed("am")
        assertFalse(config.useAppLanguage)
        assertEquals("am", config.fixedLocale.languageTag)
    }
}
