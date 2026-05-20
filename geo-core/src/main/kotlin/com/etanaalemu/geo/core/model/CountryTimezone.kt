package com.etanaalemu.geo.core.model

data class CountryTimezone(
    val zoneName: String,
    val gmtOffset: Int,
    val gmtOffsetName: String,
    val abbreviation: String,
    val tzName: String,
)
