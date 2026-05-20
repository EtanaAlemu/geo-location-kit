package com.etanaalemu.geo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.etanaalemu.geo.core.locale.GeoCatalogLanguages
import com.etanaalemu.geo.core.locale.GeoLanguageConfig

/**
 * Searchable language picker for [GeoLanguageConfig], matching the UX of [CountryCodePicker].
 *
 * @param resolvedAppLocaleTag BCP 47 tag currently resolved when using [GeoLanguageConfig.AppLanguage]
 *   (e.g. from `geoKit.locale.languageTag` after `refreshLanguage()`).
 */
@Composable
fun LanguagePicker(
    languageConfig: GeoLanguageConfig,
    resolvedAppLocaleTag: String,
    onLanguageConfigSelected: (GeoLanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val catalogTags = remember { GeoCatalogLanguages.fixedLanguageTags }
    val selectedItem = remember(languageConfig) { languageConfig.toPickerItem() }

    val featured = remember(resolvedAppLocaleTag) {
        listOf(
            FeaturedSection<LanguagePickerItem>(
                title = "App locale",
                items = listOf(LanguagePickerItem.AppLocale),
            ),
        )
    }

    SearchableDropdown(
        label = "Language",
        items = catalogTags.map { LanguagePickerItem.Catalog(it) },
        selectedItem = selectedItem,
        onItemSelected = { item ->
            onLanguageConfigSelected(item.toLanguageConfig())
        },
        itemLabel = { item ->
            when (item) {
                LanguagePickerItem.AppLocale ->
                    "System & app locale · $resolvedAppLocaleTag"
                is LanguagePickerItem.Catalog -> {
                    val tag = item.tag.replace('_', '-')
                    "${GeoCatalogLanguages.primaryTitle(item.tag)} · $tag"
                }
            }
        },
        itemKey = { item ->
            when (item) {
                LanguagePickerItem.AppLocale -> "app"
                is LanguagePickerItem.Catalog -> item.tag
            }
        },
        modifier = modifier,
        placeholder = "Search language…",
        emptyMessage = "No languages match your search",
        featuredSections = featured,
        allSectionTitle = "Catalog languages",
        searchFilter = { item, query ->
            if (query.isBlank()) return@SearchableDropdown true
            when (item) {
                LanguagePickerItem.AppLocale ->
                    "system app locale".contains(query, ignoreCase = true) ||
                        resolvedAppLocaleTag.contains(query, ignoreCase = true)
                is LanguagePickerItem.Catalog -> {
                    val tag = item.tag
                    GeoCatalogLanguages.primaryTitle(tag).contains(query, ignoreCase = true) ||
                        tag.contains(query, ignoreCase = true) ||
                        GeoCatalogLanguages.displayLabel(tag).contains(query, ignoreCase = true)
                }
            }
        },
    )
}

private sealed interface LanguagePickerItem {
    data object AppLocale : LanguagePickerItem
    data class Catalog(val tag: String) : LanguagePickerItem
}

private fun GeoLanguageConfig.toPickerItem(): LanguagePickerItem =
    if (useAppLanguage) {
        LanguagePickerItem.AppLocale
    } else {
        LanguagePickerItem.Catalog(fixedLocale.languageTag)
    }

private fun LanguagePickerItem.toLanguageConfig(): GeoLanguageConfig = when (this) {
    LanguagePickerItem.AppLocale -> GeoLanguageConfig.AppLanguage
    is LanguagePickerItem.Catalog -> GeoLanguageConfig.fixed(tag)
}
