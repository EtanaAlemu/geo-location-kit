package com.etanaalemu.geo.database

import android.content.Context
import com.etanaalemu.geo.core.GeoAttribution
import com.etanaalemu.geo.core.GeoRepository
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.database.common.locale.resolve

class GeoLocationKit private constructor(
    private val appContext: Context,
    private val repository: GeoRepositoryImpl,
) : GeoRepository by repository {

    val languageConfig: GeoLanguageConfig
        get() = repository.currentLanguageConfig()

    val locale: GeoLocale
        get() = repository.currentLocale()

    fun setLanguageConfig(config: GeoLanguageConfig) {
        repository.setLanguageConfig(config)
    }

    /** Lock to a specific language (shorthand for [setLanguageConfig]). */
    fun setLocale(locale: GeoLocale) {
        setLanguageConfig(GeoLanguageConfig.fixed(locale))
    }

    /**
     * Call when the app configuration locale may have changed (e.g. `onConfigurationChanged`).
     * Only has an effect when [languageConfig] uses [GeoLanguageConfig.AppLanguage].
     */
    fun refreshLanguage() {
        repository.refreshLanguage()
    }

    companion object {
        const val DATA_ATTRIBUTION: String = GeoAttribution.DATA_ATTRIBUTION

        @Volatile
        private var instance: GeoLocationKit? = null

        /**
         * Creates or returns the process-wide kit. Call **once** from [android.app.Application.onCreate].
         *
         * Later calls return the same instance; [languageConfig] is always applied via [setLanguageConfig].
         *
         * @param languageConfig [GeoLanguageConfig.English] by default.
         *   Use [GeoLanguageConfig.AppLanguage] to follow the app locale, or [GeoLanguageConfig.fixed] for a specific tag.
         */
        fun initialize(
            context: Context,
            languageConfig: GeoLanguageConfig = GeoLanguageConfig.English,
        ): GeoLocationKit {
            val appContext = context.applicationContext
            val kit = instance ?: synchronized(this) {
                GeoLocationKit(
                    appContext = appContext,
                    repository = GeoRepositoryImpl(
                        dao = GeoDatabaseClient.getInstance(appContext).locationDao(),
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
