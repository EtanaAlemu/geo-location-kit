package com.etanaalemu.geo.core.locale

/**
 * Language context for localized country and state names.
 *
 * Pass a BCP 47 language tag (e.g. `"en"`, `"am"`, `"fr"`, `"zh-CN"`).
 *
 * Resolution order for countries:
 * 1. English (`en`) → default English name from the dataset
 * 2. Amharic (`am`) → native name when available (e.g. ኢትዮጵያ)
 * 3. Other languages → matching entry in dr5hn `translations`, then English fallback
 *
 * States use native names for `am`, otherwise the default English name.
 * Cities are English only in the current dataset.
 */
data class GeoLocale(
    val languageTag: String,
) {
    val languageCode: String =
        languageTag.substringBefore('-').substringBefore('_').lowercase()

    companion object {
        val English: GeoLocale = GeoLocale("en")

        fun default(): GeoLocale = English

        /**
         * Build from a Java [java.util.Locale] (safe on JVM and Android).
         */
        fun from(locale: java.util.Locale): GeoLocale {
            val tag = locale.toLanguageTag()
            return GeoLocale(if (tag.isNotEmpty()) tag else locale.language)
        }
    }
}
