package com.etanaalemu.geo.database.common.locale

import android.content.Context
import com.etanaalemu.geo.core.locale.GeoLocale

/**
 * Android helpers to obtain a [GeoLocale] from the hosting app context.
 */
object GeoLocaleAndroid {
    /** Uses the app's current configuration locale (API 24+). */
    fun from(context: Context): GeoLocale = GeoLocale.from(context.resources.configuration.locales[0])

    /** Uses [Context.getResources] configuration — same as [from] for most apps. */
    fun fromApp(context: Context): GeoLocale = from(context.applicationContext)
}
