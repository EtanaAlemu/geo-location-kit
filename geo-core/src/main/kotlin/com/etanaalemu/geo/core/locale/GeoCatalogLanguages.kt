package com.etanaalemu.geo.core.locale

import java.util.Locale

/**
 * Display languages for localized **country** names (dr5hn `translations` JSON keys in the
 * shipped database, plus English and Amharic which use non-translation fields).
 *
 * State names only localize for English vs Amharic ([LocalizedNameResolver]); other tags
 * still improve the country list and picker in this demo.
 */
object GeoCatalogLanguages {

    /**
     * BCP 47 style tags used with [GeoLanguageConfig.fixed].
     * Derived from union of dr5hn country `translations` keys in the built dataset, plus `en` and `am`.
     */
    val fixedLanguageTags: List<String> = listOf(
        "en",
        "am",
        "ar",
        "br",
        "de",
        "es",
        "fa",
        "fr",
        "hi",
        "hr",
        "it",
        "ja",
        "ko",
        "nl",
        "pl",
        "pt",
        "pt-BR",
        "ru",
        "tr",
        "uk",
        "zh-CN",
    )

    fun displayLabel(tag: String, displayIn: Locale = Locale.getDefault()): String {
        val normalized = tag.replace('_', '-')
        return when (normalized.lowercase()) {
            "en" -> "English · default (database name)"
            "am" -> "አማርኛ · Amharic (native name field)"
            else -> {
                val loc = Locale.forLanguageTag(normalized)
                val name = loc.getDisplayName(displayIn)
                if (name.isNotBlank()) "$name · $normalized" else normalized
            }
        }
    }

    /** One-line title for compact pickers (list row headline). */
    fun primaryTitle(tag: String, displayIn: Locale = Locale.getDefault()): String {
        val normalized = tag.replace('_', '-')
        return when (normalized.lowercase()) {
            "en" -> "English"
            "am" -> "አማርኛ · Amharic"
            else -> {
                val loc = Locale.forLanguageTag(normalized)
                loc.getDisplayName(displayIn).ifBlank { normalized }
            }
        }
    }
}
