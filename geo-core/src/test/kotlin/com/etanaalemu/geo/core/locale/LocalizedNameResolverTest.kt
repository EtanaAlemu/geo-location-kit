package com.etanaalemu.geo.core.locale

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalizedNameResolverTest {
    private val translations = mapOf(
        "fr" to "Éthiopie",
        "de" to "Äthiopien",
        "zh-CN" to "埃塞俄比亚",
    )

    @Test
    fun resolveCountryName_englishUsesDefault() {
        val name = LocalizedNameResolver.resolveCountryName(
            defaultName = "Ethiopia",
            nativeName = "ኢትዮጵያ",
            translations = translations,
            locale = GeoLocale("en"),
        )
        assertEquals("Ethiopia", name)
    }

    @Test
    fun resolveCountryName_amharicUsesNative() {
        val name = LocalizedNameResolver.resolveCountryName(
            defaultName = "Ethiopia",
            nativeName = "ኢትዮጵያ",
            translations = translations,
            locale = GeoLocale("am"),
        )
        assertEquals("ኢትዮጵያ", name)
    }

    @Test
    fun resolveCountryName_frenchUsesTranslation() {
        val name = LocalizedNameResolver.resolveCountryName(
            defaultName = "Ethiopia",
            nativeName = "ኢትዮጵያ",
            translations = translations,
            locale = GeoLocale("fr"),
        )
        assertEquals("Éthiopie", name)
    }

    @Test
    fun resolveStateName_amharicUsesNative() {
        val name = LocalizedNameResolver.resolveStateName(
            defaultName = "Addis Ababa",
            nativeName = "አዲስ አበባ",
            locale = GeoLocale("am"),
        )
        assertEquals("አዲስ አበባ", name)
    }
}
