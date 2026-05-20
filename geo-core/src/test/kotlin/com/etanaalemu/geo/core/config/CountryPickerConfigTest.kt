package com.etanaalemu.geo.core.config

import com.etanaalemu.geo.core.model.Country
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryPickerConfigTest {
    private val countries = listOf(
        Country(1, "Ethiopia", "ET", "ETH", "+251", "ETB", "Addis Ababa"),
        Country(2, "United States", "US", "USA", "+1", "USD", "Washington"),
        Country(3, "United Kingdom", "GB", "GBR", "+44", "GBP", "London"),
        Country(4, "Kenya", "KE", "KEN", "+254", "KES", "Nairobi"),
    )

    @Test
    fun partition_preservesOrderAndExcludesFeaturedFromOther() {
        val config = CountryPickerConfig(
            featuredIso2Codes = listOf("US", "ET", "US", "XX"),
        )

        val result = config.partition(countries)

        assertEquals(listOf("US", "ET"), result.featuredCountries.map { it.iso2 })
        assertEquals(listOf("GB", "KE"), result.otherCountries.map { it.iso2 })
    }

    @Test
    fun partition_emptyConfigReturnsAllAsOther() {
        val result = CountryPickerConfig().partition(countries)
        assertTrue(result.featuredCountries.isEmpty())
        assertEquals(4, result.otherCountries.size)
    }
}
