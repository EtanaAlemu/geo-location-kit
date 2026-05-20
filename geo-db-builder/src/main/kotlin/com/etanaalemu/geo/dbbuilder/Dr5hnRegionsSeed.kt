package com.etanaalemu.geo.dbbuilder

/**
 * Static region / subregion rows from dr5hn nested export (same ids as `region_id` / `subregion_id` on countries).
 */
internal object Dr5hnRegionsSeed {
    val regions: List<Pair<Int, String>> = listOf(
        1 to "Africa",
        2 to "Americas",
        3 to "Asia",
        4 to "Europe",
        5 to "Oceania",
        6 to "Polar",
    )

    /** id, name, regionId */
    val subregions: List<Triple<Int, String, Int>> = listOf(
        Triple(1, "Northern Africa", 1),
        Triple(2, "Middle Africa", 1),
        Triple(3, "Western Africa", 1),
        Triple(4, "Eastern Africa", 1),
        Triple(5, "Southern Africa", 1),
        Triple(6, "Northern America", 2),
        Triple(7, "Caribbean", 2),
        Triple(8, "South America", 2),
        Triple(9, "Central America", 2),
        Triple(10, "Central Asia", 3),
        Triple(11, "Western Asia", 3),
        Triple(12, "Eastern Asia", 3),
        Triple(13, "South-Eastern Asia", 3),
        Triple(14, "Southern Asia", 3),
        Triple(15, "Eastern Europe", 4),
        Triple(16, "Southern Europe", 4),
        Triple(17, "Western Europe", 4),
        Triple(18, "Northern Europe", 4),
        Triple(19, "Australia and New Zealand", 5),
        Triple(20, "Melanesia", 5),
        Triple(21, "Micronesia", 5),
        Triple(22, "Polynesia", 5),
    )
}
