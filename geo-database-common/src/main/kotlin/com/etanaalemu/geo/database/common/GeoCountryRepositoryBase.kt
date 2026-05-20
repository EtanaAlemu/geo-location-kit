package com.etanaalemu.geo.database.common

import android.content.Context
import com.etanaalemu.geo.core.GeoCountryRepository
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.State
import com.etanaalemu.geo.database.common.data.CountryStateDataSource
import com.etanaalemu.geo.database.common.entity.CountryEntity
import com.etanaalemu.geo.database.common.locale.resolve
import com.etanaalemu.geo.database.common.mapper.normalizePhoneCode
import com.etanaalemu.geo.database.common.mapper.toDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
open class GeoCountryRepositoryBase(
    private val dataSource: CountryStateDataSource,
    protected val appContext: Context,
    languageConfig: GeoLanguageConfig,
) : GeoCountryRepository {
    protected val languageConfigState = MutableStateFlow(languageConfig)

    fun setLanguageConfig(config: GeoLanguageConfig) {
        languageConfigState.value = config
    }

    fun refreshLanguage() {
        languageConfigState.value = languageConfigState.value
    }

    fun currentLanguageConfig(): GeoLanguageConfig = languageConfigState.value

    fun currentLocale(): GeoLocale = languageConfigState.value.resolve(appContext)

    private fun localeNow(): GeoLocale = languageConfigState.value.resolve(appContext)

    private fun CountryEntity.toLocalizedCountry(locale: GeoLocale = localeNow()) = toDomain(locale)

    override fun getCountries(): Flow<List<Country>> =
        languageConfigState.flatMapLatest { config ->
            val locale = config.resolve(appContext)
            dataSource.getAllCountries().map { entities ->
                entities
                    .map { it.toDomain(locale) }
                    .sortedBy { it.name }
            }
        }

    override fun getStatesByCountry(countryId: Int): Flow<List<State>> =
        languageConfigState.flatMapLatest { config ->
            val locale = config.resolve(appContext)
            dataSource.getStatesByCountry(countryId).map { entities ->
                entities
                    .map { it.toDomain(locale) }
                    .sortedBy { it.name }
            }
        }

    override suspend fun getCountryById(id: Int): Country? =
        dataSource.getCountryById(id)?.toLocalizedCountry()

    override suspend fun getCountryByIso2(iso2: String): Country? {
        val normalized = iso2.trim().uppercase()
        if (normalized.length != 2) return null
        return dataSource.getCountryByIso2(normalized)?.toLocalizedCountry()
    }

    override suspend fun findCountriesByPhone(phoneCode: String): List<Country> {
        val cleanCode = normalizePhoneCode(phoneCode)
        if (cleanCode.isEmpty()) return emptyList()
        val locale = localeNow()
        return dataSource.getCountriesByPhoneCode(cleanCode)
            .map { it.toDomain(locale) }
    }

    override suspend fun findCountryByPhone(phoneCode: String, iso2Hint: String?): Country? {
        val matches = findCountriesByPhone(phoneCode)
        if (matches.isEmpty()) return null
        if (!iso2Hint.isNullOrBlank()) {
            matches.firstOrNull { it.iso2.equals(iso2Hint.trim(), ignoreCase = true) }
                ?.let { return it }
        }
        return matches.first()
    }
}
