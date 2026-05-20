package com.etanaalemu.geo.core.config

import com.etanaalemu.geo.core.model.Country

/**
 * Configures countries pinned at the top of country pickers (above the full list).
 *
 * Use ISO 3166-1 alpha-2 codes in display order (e.g. `"ET"`, `"US"`).
 * Codes not found in the dataset are skipped.
 */
data class CountryPickerConfig(
    val featuredIso2Codes: List<String> = emptyList(),
    val featuredSectionTitle: String = "Featured",
) {
    fun isEmpty(): Boolean = featuredIso2Codes.isEmpty()
}

data class PartitionedCountries(
    val featuredCountries: List<Country>,
    val otherCountries: List<Country>,
)

fun CountryPickerConfig.partition(countries: List<Country>): PartitionedCountries {
    if (isEmpty()) {
        return PartitionedCountries(featuredCountries = emptyList(), otherCountries = countries)
    }

    val byIso2 = countries.associateBy { it.iso2.uppercase() }

    val featured = featuredIso2Codes
        .map { it.uppercase() }
        .distinct()
        .mapNotNull(byIso2::get)

    val featuredIds = featured.map { it.id }.toSet()
    val otherCountries = countries.filter { it.id !in featuredIds }

    return PartitionedCountries(
        featuredCountries = featured,
        otherCountries = otherCountries,
    )
}
