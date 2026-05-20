package com.etanaalemu.geo.database.lite

import android.content.Context
import com.etanaalemu.geo.core.GeoAttribution
import com.etanaalemu.geo.core.GeoCountryRepository
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.database.common.locale.resolve

/**
 * Entry point for the **lite** database: countries, states/regions, phone codes, and region/subregion
 * metadata (~1–2 MB asset, no cities or postcodes).
 */
class GeoLocationKitLite private constructor(
    private val appContext: Context,
    private val repository: GeoCountryRepositoryLiteImpl,
) : GeoCountryRepository by repository {

    val languageConfig: GeoLanguageConfig
        get() = repository.currentLanguageConfig()

    val locale: GeoLocale
        get() = repository.currentLocale()

    fun setLanguageConfig(config: GeoLanguageConfig) {
        repository.setLanguageConfig(config)
    }

    fun setLocale(locale: GeoLocale) {
        setLanguageConfig(GeoLanguageConfig.fixed(locale))
    }

    fun refreshLanguage() {
        repository.refreshLanguage()
    }

    companion object {
        const val DATA_ATTRIBUTION: String = GeoAttribution.DATA_ATTRIBUTION

        @Volatile
        private var instance: GeoLocationKitLite? = null

        /**
         * Creates or returns the process-wide lite kit. Call **once** from [android.app.Application.onCreate].
         */
        fun initialize(
            context: Context,
            languageConfig: GeoLanguageConfig = GeoLanguageConfig.English,
        ): GeoLocationKitLite {
            val appContext = context.applicationContext
            val kit = instance ?: synchronized(this) {
                GeoLocationKitLite(
                    appContext = appContext,
                    repository = GeoCountryRepositoryLiteImpl(
                        dao = GeoDatabaseLiteClient.getInstance(appContext).locationLiteDao(),
                        appContext = appContext,
                        languageConfig = languageConfig,
                    ),
                ).also { instance = it }
            }
            kit.setLanguageConfig(languageConfig)
            return kit
        }
    }
}
