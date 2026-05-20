package com.etanaalemu.geo.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.etanaalemu.geo.core.config.CountryPickerConfig
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.util.FlagEmoji

@Composable
fun CountryCodePicker(
    countries: List<Country>,
    selectedCountry: Country?,
    onCountrySelected: (Country) -> Unit,
    modifier: Modifier = Modifier,
    pickerConfig: CountryPickerConfig = CountryPickerConfig(),
) {
    val itemLabel: (Country) -> String = {
        "${FlagEmoji.fromIso2(it.iso2)} ${it.name} (${it.formattedPhoneCode})"
    }

    val (featuredSections, otherCountries) = remember(countries, pickerConfig) {
        if (pickerConfig.isEmpty()) {
            emptyList<FeaturedSection<Country>>() to countries
        } else {
            pickerConfig.toFeaturedSections(countries)
        }
    }

    SearchableDropdown(
        label = "Country Code",
        items = otherCountries,
        selectedItem = selectedCountry,
        onItemSelected = onCountrySelected,
        itemLabel = itemLabel,
        itemKey = { it.iso2 },
        modifier = modifier,
        placeholder = "Search country or code…",
        featuredSections = featuredSections,
        allSectionTitle = "All countries",
        searchFilter = { country, query ->
            country.name.contains(query, ignoreCase = true) ||
                country.formattedPhoneCode.contains(query) ||
                country.iso2.contains(query, ignoreCase = true) ||
                country.iso3.contains(query, ignoreCase = true)
        },
    )
}
