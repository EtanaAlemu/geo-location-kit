package com.etanaalemu.geo.core.model

data class Postcode(
    val id: Int,
    val code: String,
    val countryId: Int,
    val countryCode: String,
    val stateId: Int,
    val stateCode: String,
    val cityId: Int?,
    val localityName: String,
    val type: String,
    val latitude: Double?,
    val longitude: Double?,
    val source: String,
    val wikiDataId: String?,
)
