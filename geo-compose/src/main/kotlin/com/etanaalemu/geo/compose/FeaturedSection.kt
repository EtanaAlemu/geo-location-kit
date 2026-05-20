package com.etanaalemu.geo.compose

/**
 * A labeled group of items pinned to the top of a [SearchableDropdown] dialog.
 */
data class FeaturedSection<T>(
    val title: String,
    val items: List<T>,
)
