package com.etanaalemu.geo.database

import android.content.Context
import com.etanaalemu.geo.core.GeoRepository
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.CountryTimezone
import com.etanaalemu.geo.core.model.Postcode
import com.etanaalemu.geo.core.model.Region
import com.etanaalemu.geo.core.model.Subregion
import com.etanaalemu.geo.database.common.GeoCountryRepositoryBase
import com.etanaalemu.geo.database.common.locale.resolve
import com.etanaalemu.geo.database.common.mapper.toDomain
import com.etanaalemu.geo.database.dao.LocationDao
import com.etanaalemu.geo.database.data.LocationDaoDataSource
import com.etanaalemu.geo.database.mapper.toCountryTimezoneDomain
import com.etanaalemu.geo.database.mapper.toDomain
import com.etanaalemu.geo.database.mapper.toPostcodeDomain
import com.etanaalemu.geo.database.search.FtsCityQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
internal class GeoRepositoryImpl(
    private val dao: LocationDao,
    appContext: Context,
    languageConfig: GeoLanguageConfig,
) : GeoCountryRepositoryBase(LocationDaoDataSource(dao), appContext, languageConfig), GeoRepository {

    override fun searchCitiesInState(stateId: Int, query: String): Flow<List<City>> {
        val trimmed = query.trim()
        return languageConfigState.flatMapLatest { config ->
            val locale = config.resolve(appContext)
            val entityFlow = if (trimmed.isEmpty()) {
                dao.searchCitiesInStatePrefix(stateId, "")
            } else {
                val match = FtsCityQuery.toPrefixMatchQuery(trimmed)
                if (match != null) {
                    dao.searchCitiesInStateFts(stateId, match)
                } else {
                    dao.searchCitiesInStatePrefix(stateId, trimmed)
                }
            }
            entityFlow.map { entities -> entities.map { it.toDomain(locale) } }
        }
    }

    override suspend fun searchCitiesInStatePage(
        stateId: Int,
        query: String,
        limit: Int,
        offset: Int,
    ): List<City> {
        val trimmed = query.trim()
        val locale = languageConfigState.value.resolve(appContext)
        val entities = if (trimmed.isEmpty()) {
            dao.searchCitiesInStatePrefixPage(stateId, "", limit, offset)
        } else {
            val match = FtsCityQuery.toPrefixMatchQuery(trimmed)
            if (match != null) {
                dao.searchCitiesInStateFtsPage(stateId, match, limit, offset)
            } else {
                dao.searchCitiesInStatePrefixPage(stateId, trimmed, limit, offset)
            }
        }
        return entities.map { it.toDomain(locale) }
    }

    override fun getRegions(): Flow<List<Region>> =
        dao.getAllRegions().map { list -> list.map { it.toDomain() } }

    override fun getSubregions(regionId: Int): Flow<List<Subregion>> =
        dao.getSubregionsByRegion(regionId).map { list -> list.map { it.toDomain() } }

    override suspend fun getCountryTimezones(countryId: Int): List<CountryTimezone> =
        dao.getCountryTimezones(countryId).map { it.toCountryTimezoneDomain() }

    override suspend fun searchPostcodes(countryId: Int, codePrefix: String, limit: Int): List<Postcode> {
        val prefix = codePrefix.trim()
        if (prefix.isEmpty()) return emptyList()
        return dao.searchPostcodesByCountryAndCodePrefix(countryId, prefix, limit)
            .map { it.toPostcodeDomain() }
    }
}
