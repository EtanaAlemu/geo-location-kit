package com.etanaalemu.geo.core.locale

/**
 * Controls how the library picks a language for country and state names.
 *
 * - [English] — always English (default)
 * - [AppLanguage] — follow the hosting app's current configuration locale
 * - [fixed] — lock to a specific BCP 47 tag (e.g. `"am"`, `"fr"`)
 */
data class GeoLanguageConfig(
    val useAppLanguage: Boolean = false,
    val fixedLocale: GeoLocale = GeoLocale.English,
) {
    companion object {
        /** Default: always English names. */
        val English: GeoLanguageConfig = GeoLanguageConfig(
            useAppLanguage = false,
            fixedLocale = GeoLocale.English,
        )

        /** Follow the app's configuration locale (Android: [android.content.res.Configuration]). */
        val AppLanguage: GeoLanguageConfig = GeoLanguageConfig(
            useAppLanguage = true,
            fixedLocale = GeoLocale.English,
        )

        /** Lock to a specific language tag. */
        fun fixed(languageTag: String): GeoLanguageConfig = GeoLanguageConfig(
            useAppLanguage = false,
            fixedLocale = GeoLocale(languageTag),
        )

        fun fixed(locale: GeoLocale): GeoLanguageConfig = GeoLanguageConfig(
            useAppLanguage = false,
            fixedLocale = locale,
        )
    }
}
