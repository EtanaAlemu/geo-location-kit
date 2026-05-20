package com.etanaalemu.geo.dbbuilder

import com.google.gson.annotations.SerializedName

internal data class CountryJson(
    val id: Int,
    val name: String,
    val iso2: String,
    val iso3: String,
    @SerializedName("phone_code") val phoneCodeSnake: String? = null,
    @SerializedName("phonecode") val phoneCodeCamel: String? = null,
    val currency: String?,
    val capital: String?,
    val native: String? = null,
    val translations: Map<String, String>? = null,
) {
    val phoneCode: String
        get() = phoneCodeSnake ?: phoneCodeCamel.orEmpty()
}

internal data class StateJson(
    val id: Int,
    val name: String,
    @SerializedName("country_id") val countryId: Int = 0,
    @SerializedName("state_code") val stateCode: String? = null,
    val iso2: String? = null,
    val native: String? = null,
)

internal data class CityJson(
    val id: Int,
    val name: String,
    @SerializedName("state_id") val stateId: Int = 0,
    @SerializedName("country_id") val countryId: Int = 0,
    val latitude: String?,
    val longitude: String?,
    val timezone: String? = null,
)

internal data class PostcodeJson(
    val id: Int,
    val code: String,
    @SerializedName("country_id") val countryId: Int = 0,
    @SerializedName("country_code") val countryCode: String? = null,
    @SerializedName("state_id") val stateId: Int? = null,
    @SerializedName("state_code") val stateCode: String? = null,
    @SerializedName("city_id") val cityId: Int? = null,
    @SerializedName("locality_name") val localityName: String? = null,
    val type: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val source: String? = null,
    @SerializedName("wikiDataId") val wikiDataId: String? = null,
)
