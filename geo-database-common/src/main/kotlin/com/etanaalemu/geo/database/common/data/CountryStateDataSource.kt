package com.etanaalemu.geo.database.common.data

import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import kotlinx.coroutines.flow.Flow

interface CountryStateDataSource {
    fun getAllCountries(): Flow<List<CountryEntity>>

    fun getStatesByCountry(countryId: Int): Flow<List<StateEntity>>

    suspend fun getCountryById(id: Int): CountryEntity?

    suspend fun getCountryByIso2(iso2: String): CountryEntity?

    suspend fun getCountriesByPhoneCode(phoneCode: String): List<CountryEntity>
}
