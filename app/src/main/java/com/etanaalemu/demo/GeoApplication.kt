package com.etanaalemu.demo

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.locale.GeoLocale
import com.etanaalemu.geo.database.GeoLocationKit

class GeoApplication : Application() {
    lateinit var geoKit: GeoLocationKit
        private set

    override fun onCreate() {
        super.onCreate()
        val languageConfig = initialLanguageConfigFromAppLocales()
        geoKit = GeoLocationKit.initialize(this, languageConfig = languageConfig)
    }

    /**
     * Aligns [GeoLocationKit] with Android per-app locales ([AppCompatDelegate.getApplicationLocales]).
     * When none are set, country names follow the **system** configuration ([GeoLanguageConfig.AppLanguage]).
     */
    private fun initialLanguageConfigFromAppLocales(): GeoLanguageConfig {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            val primary = locales[0] ?: return GeoLanguageConfig.AppLanguage
            GeoLanguageConfig.fixed(GeoLocale.from(primary))
        } else {
            GeoLanguageConfig.AppLanguage
        }
    }
}
