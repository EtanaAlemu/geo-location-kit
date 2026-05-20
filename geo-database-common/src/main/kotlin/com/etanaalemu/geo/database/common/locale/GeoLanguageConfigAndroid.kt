package com.etanaalemu.geo.database.common.locale

import android.content.Context
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.locale.GeoLocale

/** Resolves [GeoLanguageConfig] to a concrete [GeoLocale] on Android. */
fun GeoLanguageConfig.resolve(context: Context): GeoLocale {
    return if (useAppLanguage) {
        GeoLocaleAndroid.fromApp(context)
    } else {
        fixedLocale
    }
}
