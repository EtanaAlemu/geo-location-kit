package com.etanaalemu.geo.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.etanaalemu.geo.core.config.CountryPickerConfig
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.State
import com.etanaalemu.geo.core.util.FlagEmoji

/**
 * Cascading country → state → city selectors.
 *
 * Selection is controlled by the parent via [selectedCountry], [selectedState], and [selectedCity].
 */
@Composable
fun CascadingLocationSelector(
    countries: List<Country>,
    states: List<State>,
    cities: List<City>,
    citySearchQuery: String,
    selectedCountry: Country?,
    selectedState: State?,
    selectedCity: City?,
    onCountryChanged: (Country) -> Unit,
    onStateChanged: (State) -> Unit,
    onCityChanged: (City) -> Unit,
    onCitySearchQueryChange: (String) -> Unit,
    cityHasMore: Boolean = false,
    cityLoadingMore: Boolean = false,
    onLoadMoreCities: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    countryPickerConfig: CountryPickerConfig = CountryPickerConfig(),
) {
    val countryItemLabel: (Country) -> String = {
        "${FlagEmoji.fromIso2(it.iso2)} ${it.name}"
    }

    val (countryFeaturedSections, otherCountries) = remember(countries, countryPickerConfig) {
        if (countryPickerConfig.isEmpty()) {
            emptyList<FeaturedSection<Country>>() to countries
        } else {
            countryPickerConfig.toFeaturedSections(countries)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SearchableDropdown(
            label = "Country",
            items = otherCountries,
            selectedItem = selectedCountry,
            itemLabel = countryItemLabel,
            itemKey = { it.iso2 },
            placeholder = "Search country…",
            featuredSections = countryFeaturedSections,
            allSectionTitle = "All countries",
            searchFilter = { country, query ->
                country.name.contains(query, ignoreCase = true) ||
                    country.iso2.contains(query, ignoreCase = true) ||
                    country.iso3.contains(query, ignoreCase = true)
            },
            onItemSelected = onCountryChanged,
        )

        SearchableDropdown(
            label = "State/Region",
            items = states,
            selectedItem = selectedState,
            enabled = selectedCountry != null,
            itemLabel = { it.name },
            itemKey = { it.id },
            placeholder = if (selectedCountry != null) "Search state…" else "Select country first",
            emptyMessage = "No states found",
            searchFilter = { state, query ->
                state.name.contains(query, ignoreCase = true) ||
                    state.stateCode.contains(query, ignoreCase = true)
            },
            onItemSelected = onStateChanged,
        )

        SearchableDropdown(
            label = "City",
            items = cities,
            selectedItem = selectedCity,
            enabled = selectedState != null,
            itemLabel = { it.name },
            itemKey = { it.id },
            placeholder = if (selectedState != null) "Search city…" else "Select state first",
            emptyMessage = "No cities found. Type to search (prefix match).",
            onSearchQueryChange = onCitySearchQueryChange,
            externalSearchQuery = citySearchQuery,
            hasMoreItems = cityHasMore,
            isLoadingMore = cityLoadingMore,
            onLoadMore = onLoadMoreCities,
            onItemSelected = onCityChanged,
        )
    }
}
