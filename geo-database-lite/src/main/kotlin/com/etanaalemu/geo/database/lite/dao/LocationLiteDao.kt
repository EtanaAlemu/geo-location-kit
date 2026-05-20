package com.etanaalemu.geo.database.lite.dao

import androidx.room.Dao
import androidx.room.Query
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationLiteDao {
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

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun getCountryCount(): Int
}
