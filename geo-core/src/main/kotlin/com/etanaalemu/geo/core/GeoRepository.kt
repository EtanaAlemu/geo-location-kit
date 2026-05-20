package com.etanaalemu.geo.core

import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.CountryTimezone
import com.etanaalemu.geo.core.model.Postcode
import com.etanaalemu.geo.core.model.Region
import com.etanaalemu.geo.core.model.Subregion
import kotlinx.coroutines.flow.Flow

interface GeoRepository : GeoCountryRepository {
    /**
     * City search within a state, max **50** results, ordered by name (first page only).
     *
     * - **Empty query**: first 50 cities A–Z (prefix `LIKE` on `name`).
     * - **Non-empty**: FTS4 token/prefix search on `name` (multi-word = AND of prefixes). Tokens shorter than 2 characters fall back to prefix `LIKE`.
     *
     * For infinite scroll in UI, use [searchCitiesInStatePage] with increasing [offset].
     */
    fun searchCitiesInState(stateId: Int, query: String): Flow<List<City>>

    /**
     * Paginated city search within a state. Returns up to [limit] rows starting at [offset].
     * Request the next page when the previous result size equals [limit].
     */
    suspend fun searchCitiesInStatePage(
        stateId: Int,
        query: String,
        limit: Int = 50,
        offset: Int = 0,
    ): List<City>

    /**
     * First 50 cities in a state, A–Z. Same as [searchCitiesInState] with an empty query.
     */
    fun getTopCitiesInState(stateId: Int): Flow<List<City>> = searchCitiesInState(stateId, "")

    /** All geographic regions (e.g. Africa, Europe), ordered by name. */
    fun getRegions(): Flow<List<Region>>

    /** Subregions for a [regionId] (e.g. Northern Europe), ordered by name. */
    fun getSubregions(regionId: Int): Flow<List<Subregion>>

    /** All IANA timezones for a country (from dr5hn nested export). */
    suspend fun getCountryTimezones(countryId: Int): List<CountryTimezone>

    /**
     * Postcode prefix search within a country (from `json-postcodes.json.gz`).
     * [codePrefix] is matched with `LIKE prefix%` (case-sensitive as stored in SQLite).
     */
    suspend fun searchPostcodes(countryId: Int, codePrefix: String, limit: Int = 50): List<Postcode>
}
