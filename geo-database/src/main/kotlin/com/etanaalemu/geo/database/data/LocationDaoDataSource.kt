package com.etanaalemu.geo.database.data

import com.etanaalemu.geo.database.common.data.CountryStateDataSource
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.entity.StateEntity
import com.etanaalemu.geo.database.dao.LocationDao
import kotlinx.coroutines.flow.Flow

internal class LocationDaoDataSource(
    private val dao: LocationDao,
) : CountryStateDataSource {
    override fun getAllCountries(): Flow<List<CountryEntity>> = dao.getAllCountries()

    override fun getStatesByCountry(countryId: Int): Flow<List<StateEntity>> =
        dao.getStatesByCountry(countryId)

    override suspend fun getCountryById(id: Int): CountryEntity? = dao.getCountryById(id)

    override suspend fun getCountryByIso2(iso2: String): CountryEntity? = dao.getCountryByIso2(iso2)

    override suspend fun getCountriesByPhoneCode(phoneCode: String): List<CountryEntity> =
        dao.getCountriesByPhoneCode(phoneCode)
}
