package com.etanaalemu.geo.core.model

data class Country(
    val id: Int,
    val name: String,
    val iso2: String,
    val iso3: String,
    val formattedPhoneCode: String,
    val currency: String,
    val capital: String,
    val regionId: Int = 0,
    val subregionId: Int = 0,
    val currencyName: String = "",
    val currencySymbol: String = "",
    val emoji: String = "",
    val emojiUnicode: String = "",
    val nationality: String = "",
    val numericCode: String = "",
    val population: Long = 0L,
    val gdp: String = "",
    val tld: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    /** Raw dr5hn `translations` JSON (same as DB column). */
    val translationsJson: String = "{}",
)
