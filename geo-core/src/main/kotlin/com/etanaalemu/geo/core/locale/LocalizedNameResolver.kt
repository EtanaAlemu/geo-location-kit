package com.etanaalemu.geo.core.locale

/**
 * Resolves display names from dr5hn default names, native names, and translation maps.
 */
object LocalizedNameResolver {
    fun resolveCountryName(
        defaultName: String,
        nativeName: String?,
        translations: Map<String, String>,
        locale: GeoLocale,
    ): String {
        if (locale.languageCode == "en") return defaultName

        if (locale.languageCode == "am" && !nativeName.isNullOrBlank()) {
            return nativeName
        }

        lookupTranslation(translations, locale)?.let { return it }

        return defaultName
    }

    fun resolveStateName(
        defaultName: String,
        nativeName: String?,
        locale: GeoLocale,
    ): String {
        if (locale.languageCode == "en") return defaultName
        if (locale.languageCode == "am" && !nativeName.isNullOrBlank()) return nativeName
        return defaultName
    }

    fun resolveCityName(defaultName: String, locale: GeoLocale): String = defaultName

    internal fun lookupTranslation(
        translations: Map<String, String>,
        locale: GeoLocale,
    ): String? {
        if (translations.isEmpty()) return null

        val candidates = buildList {
            add(locale.languageTag)
            add(locale.languageTag.replace('_', '-'))
            add(mapToDr5hnTranslationKey(locale.languageTag))
            if (locale.languageTag.contains('-') || locale.languageTag.contains('_')) {
                add(locale.languageCode)
            }
        }

        for (key in candidates.distinct()) {
            translations[key]?.let { return it }
            translations[key.lowercase()]?.let { return it }
        }
        return null
    }

    /**
     * Maps BCP 47 tags to keys used in the dr5hn countries `translations` object.
     */
    fun mapToDr5hnTranslationKey(languageTag: String): String {
        val normalized = languageTag.replace('_', '-').lowercase()
        return when (normalized) {
            "zh", "zh-hans", "zh-cn" -> "zh-CN"
            "pt-br" -> "pt-BR"
            else -> languageTag.replace('_', '-')
        }
    }
}
