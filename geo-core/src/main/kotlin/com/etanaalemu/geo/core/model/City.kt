package com.etanaalemu.geo.core.model

data class City(
    val id: Int,
    val name: String,
    val stateId: Int,
    val countryId: Int,
    val latitude: Double,
    val longitude: Double,
    val timezone: String = "",
)
