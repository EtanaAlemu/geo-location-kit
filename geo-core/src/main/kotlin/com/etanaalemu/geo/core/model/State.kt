package com.etanaalemu.geo.core.model

data class State(
    val id: Int,
    val name: String,
    val countryId: Int,
    val stateCode: String,
    val timezone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "",
    val iso3166_2: String = "",
)
