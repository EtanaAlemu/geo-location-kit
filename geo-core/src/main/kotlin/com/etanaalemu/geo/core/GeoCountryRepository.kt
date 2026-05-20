package com.etanaalemu.geo.core

import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.State
import kotlinx.coroutines.flow.Flow

/**
 * Country, state/region, and phone-code APIs without city data.
 *
 * Implemented by [GeoRepository] (full database) and [com.etanaalemu.geo.database.lite.GeoLocationKitLite].
 */
interface GeoCountryRepository {
    fun getCountries(): Flow<List<Country>>

    fun getStatesByCountry(countryId: Int): Flow<List<State>>

    suspend fun getCountryById(id: Int): Country?

    suspend fun getCountryByIso2(iso2: String): Country?

    /** All countries sharing a phone code, ordered by display name. */
    suspend fun findCountriesByPhone(phoneCode: String): List<Country>

    /**
     * Resolves a country by phone code. When several countries share a code (e.g. +1),
     * pass [iso2Hint] to pick the intended one (e.g. `"US"`).
     */
    suspend fun findCountryByPhone(phoneCode: String, iso2Hint: String? = null): Country?
}
