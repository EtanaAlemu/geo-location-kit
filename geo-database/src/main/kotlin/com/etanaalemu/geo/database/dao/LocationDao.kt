package com.etanaalemu.geo.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.RegionEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import com.etanaalemu.geo.database.common.entity.SubregionEntity
import com.etanaalemu.geo.database.entity.CityEntity
import com.etanaalemu.geo.database.entity.CountryTimezoneEntity
import com.etanaalemu.geo.database.entity.PostcodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM countries ORDER BY name ASC")
    fun getAllCountries(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries WHERE id = :id LIMIT 1")
    suspend fun getCountryById(id: Int): CountryEntity?

    @Query("SELECT * FROM countries WHERE iso2 = :iso2 COLLATE NOCASE LIMIT 1")
    suspend fun getCountryByIso2(iso2: String): CountryEntity?

    @Query("SELECT * FROM countries WHERE phoneCode = :phoneCode ORDER BY name ASC")
    suspend fun getCountriesByPhoneCode(phoneCode: String): List<CountryEntity>

    @Query("SELECT * FROM states WHERE countryId = :countryId ORDER BY name ASC")
    fun getStatesByCountry(countryId: Int): Flow<List<StateEntity>>

    /**
     * Prefix match on the `name` column (legacy path). Used when the search string is empty
     * (returns first 50 cities A–Z in the state).
     */
    @Query(
        """
        SELECT * FROM cities
        WHERE stateId = :stateId AND name LIKE :searchQuery || '%'
        ORDER BY name ASC
        LIMIT 50
        """,
    )
    fun searchCitiesInStatePrefix(stateId: Int, searchQuery: String): Flow<List<CityEntity>>

    @Query(
        """
        SELECT * FROM cities
        WHERE stateId = :stateId AND name LIKE :searchQuery || '%'
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchCitiesInStatePrefixPage(
        stateId: Int,
        searchQuery: String,
        limit: Int,
        offset: Int,
    ): List<CityEntity>

    /**
     * Token/prefix FTS4 search within a state. [matchQuery] must be built by [com.etanaalemu.geo.database.search.FtsCityQuery].
     */
    @Query(
        """
        SELECT cities.* FROM cities
        INNER JOIN cities_fts ON cities.rowid = cities_fts.docid
        WHERE cities.stateId = :stateId AND cities_fts MATCH :matchQuery
        ORDER BY cities.name ASC
        LIMIT 50
        """,
    )
    fun searchCitiesInStateFts(stateId: Int, matchQuery: String): Flow<List<CityEntity>>

    @Query(
        """
        SELECT cities.* FROM cities
        INNER JOIN cities_fts ON cities.rowid = cities_fts.docid
        WHERE cities.stateId = :stateId AND cities_fts MATCH :matchQuery
        ORDER BY cities.name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun searchCitiesInStateFtsPage(
        stateId: Int,
        matchQuery: String,
        limit: Int,
        offset: Int,
    ): List<CityEntity>

    @Query("SELECT * FROM regions ORDER BY name ASC")
    fun getAllRegions(): Flow<List<RegionEntity>>

    @Query("SELECT * FROM subregions WHERE regionId = :regionId ORDER BY name ASC")
    fun getSubregionsByRegion(regionId: Int): Flow<List<SubregionEntity>>

    @Query(
        """
        SELECT * FROM country_timezones
        WHERE countryId = :countryId
        ORDER BY zoneName ASC
        """,
    )
    suspend fun getCountryTimezones(countryId: Int): List<CountryTimezoneEntity>

    @Query(
        """
        SELECT * FROM postcodes
        WHERE countryId = :countryId AND code LIKE :codePrefix || '%'
        ORDER BY code ASC
        LIMIT :limit
        """,
    )
    suspend fun searchPostcodesByCountryAndCodePrefix(
        countryId: Int,
        codePrefix: String,
        limit: Int,
    ): List<PostcodeEntity>

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountryCount(): Int
}
