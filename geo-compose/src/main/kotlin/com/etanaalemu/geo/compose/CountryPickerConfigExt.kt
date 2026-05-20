package com.etanaalemu.geo.compose

import com.etanaalemu.geo.core.config.CountryPickerConfig
import com.etanaalemu.geo.core.config.partition
import com.etanaalemu.geo.core.model.Country

internal fun CountryPickerConfig.toFeaturedSections(
    countries: List<Country>,
): Pair<List<FeaturedSection<Country>>, List<Country>> {
    val partitioned = partition(countries)
    if (partitioned.featuredCountries.isEmpty()) {
        return emptyList<FeaturedSection<Country>>() to partitioned.otherCountries
    }
    val section = FeaturedSection(
        title = featuredSectionTitle,
        items = partitioned.featuredCountries,
    )
    return listOf(section) to partitioned.otherCountries
}
