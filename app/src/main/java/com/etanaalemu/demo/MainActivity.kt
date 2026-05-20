package com.etanaalemu.demo

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.etanaalemu.demo.ui.theme.GeoLocationKitTheme
import com.etanaalemu.geo.compose.CascadingLocationSelector
import com.etanaalemu.geo.compose.CountryCodePicker
import com.etanaalemu.geo.compose.LanguagePicker
import com.etanaalemu.geo.compose.LocationFormViewModel
import com.etanaalemu.geo.core.config.CountryPickerConfig
import com.etanaalemu.geo.core.locale.GeoLanguageConfig
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.CountryTimezone
import com.etanaalemu.geo.core.model.Postcode
import com.etanaalemu.geo.core.model.State
import com.etanaalemu.geo.database.GeoLocationKit

class MainActivity : AppCompatActivity() {
    private lateinit var geoApp: GeoApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geoApp = application as GeoApplication
        enableEdgeToEdge()
        setContent {
            GeoLocationKitTheme {
                DemoScreen(geoApp = geoApp)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::geoApp.isInitialized && geoApp.geoKit.languageConfig.useAppLanguage) {
            geoApp.geoKit.refreshLanguage()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoScreen(geoApp: GeoApplication) {
    val viewModel: LocationFormViewModel = viewModel(
        factory = LocationFormViewModelFactory(geoApp.geoKit),
    )

    val countries by viewModel.countries.collectAsStateWithLifecycle()
    val states by viewModel.states.collectAsStateWithLifecycle()
    val cities by viewModel.cities.collectAsStateWithLifecycle()
    val selectedCountry by viewModel.selectedCountry.collectAsStateWithLifecycle()
    val selectedState by viewModel.selectedState.collectAsStateWithLifecycle()
    val selectedCity by viewModel.selectedCity.collectAsStateWithLifecycle()
    val citySearchQuery by viewModel.citySearchQuery.collectAsStateWithLifecycle()
    val cityHasMore by viewModel.cityHasMore.collectAsStateWithLifecycle()
    val cityLoadingMore by viewModel.cityLoadingMore.collectAsStateWithLifecycle()
    val locationSummary by viewModel.locationSummary.collectAsStateWithLifecycle()

    var selectedPhoneCountry by remember { mutableStateOf<Country?>(null) }
    var localeRefreshTick by remember { mutableStateOf(0) }

    val detailCountry = selectedCountry ?: selectedPhoneCountry
    val regions by geoApp.geoKit.getRegions().collectAsStateWithLifecycle(initialValue = emptyList())
    val regionId = detailCountry?.regionId ?: 0
    val subregions by remember(regionId) {
        geoApp.geoKit.getSubregions(regionId)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    var countryTimezones by remember { mutableStateOf<List<CountryTimezone>>(emptyList()) }
    LaunchedEffect(detailCountry?.id) {
        val id = detailCountry?.id
        countryTimezones = if (id != null) geoApp.geoKit.getCountryTimezones(id) else emptyList()
    }

    var postcodePrefix by remember { mutableStateOf("") }
    var postcodeHits by remember { mutableStateOf<List<Postcode>>(emptyList()) }
    LaunchedEffect(detailCountry?.id, postcodePrefix) {
        val id = detailCountry?.id
        val q = postcodePrefix.trim()
        postcodeHits = if (id != null && q.length >= 2) {
            geoApp.geoKit.searchPostcodes(id, q, limit = 25)
        } else {
            emptyList()
        }
    }

    val countryPickerConfig = remember {
        CountryPickerConfig(
            featuredIso2Codes = listOf("ET", "US", "GB", "CA", "DE"),
            featuredSectionTitle = "Popular",
        )
    }

    var languageConfig by remember { mutableStateOf(geoApp.geoKit.languageConfig) }

    val resolvedLocaleTag = remember(languageConfig, localeRefreshTick) {
        geoApp.geoKit.locale.languageTag
    }
    val localeLabel = remember(languageConfig, resolvedLocaleTag) {
        if (languageConfig.useAppLanguage) {
            "Country names follow app locale · $resolvedLocaleTag"
        } else {
            "Fixed locale · ${languageConfig.fixedLocale.languageTag}"
        }
    }

    var inspectorExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Geo Location Kit",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = GeoLocationKit.DATA_ATTRIBUTION,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                DemoSectionCard(
                    title = "Language",
                    subtitle = localeLabel,
                ) {
                    Text(
                        text = "Tap to open a searchable list (same pattern as country picker). " +
                            "System & app locale follows Android; fixed tags also set per-app language in this demo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LanguagePicker(
                        languageConfig = languageConfig,
                        resolvedAppLocaleTag = resolvedLocaleTag,
                        onLanguageConfigSelected = { config ->
                            applyAppAndGeoLanguage(geoApp, config)
                            languageConfig = config
                            localeRefreshTick++
                        },
                    )
                }
            }

            item {
                DemoSectionCard(
                    title = "Phone & country",
                    subtitle = "Dial-code picker (library sample)",
                ) {
                    CountryCodePicker(
                        countries = countries,
                        selectedCountry = selectedPhoneCountry,
                        onCountrySelected = { selectedPhoneCountry = it },
                        pickerConfig = countryPickerConfig,
                    )
                }
            }

            item {
                DemoSectionCard(
                    title = "Address cascade",
                    subtitle = "Country → state → city with search",
                ) {
                    CascadingLocationSelector(
                        countries = countries,
                        states = states,
                        cities = cities,
                        citySearchQuery = citySearchQuery,
                        selectedCountry = selectedCountry,
                        selectedState = selectedState,
                        selectedCity = selectedCity,
                        countryPickerConfig = countryPickerConfig,
                        onCountryChanged = viewModel::selectCountry,
                        onStateChanged = viewModel::selectState,
                        onCityChanged = viewModel::selectCity,
                        onCitySearchQueryChange = viewModel::updateCitySearchQuery,
                        cityHasMore = cityHasMore,
                        cityLoadingMore = cityLoadingMore,
                        onLoadMoreCities = viewModel::loadMoreCities,
                    )
                    if (locationSummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = locationSummary,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(Modifier.padding(4.dp)) {
                        TextButton(
                            onClick = { inspectorExpanded = !inspectorExpanded },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = if (inspectorExpanded) "Hide model inspector" else "Show model inspector",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = if (inspectorExpanded) "▲" else "▼",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        if (inspectorExpanded) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                            )
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                if (detailCountry == null) {
                                    Text(
                                        text = "Select a country from the picker or cascade above to inspect DB-backed fields.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    CountryFieldsBlock(country = detailCountry)

                                    Text(
                                        text = "Regions (${regions.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    for (r in regions) {
                                        Text(
                                            text = "${r.id} · ${r.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }

                                    Text(
                                        text = "Subregions (region ${detailCountry.regionId})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    if (subregions.isEmpty()) {
                                        Text(
                                            text = "No rows (region id is 0 or unknown).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                    } else {
                                        for (s in subregions) {
                                            Text(
                                                text = "${s.id} · ${s.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Country timezones",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    if (countryTimezones.isEmpty()) {
                                        Text(
                                            text = "None",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline,
                                        )
                                    } else {
                                        for (tz in countryTimezones) {
                                            Text(
                                                text = "${tz.zoneName} · ${tz.gmtOffsetName} (${tz.abbreviation}, ${tz.gmtOffset}s) — ${tz.tzName}",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = postcodePrefix,
                                        onValueChange = { postcodePrefix = it },
                                        label = { Text("Postcode prefix (2+ chars)") },
                                        supportingText = {
                                            Text("${postcodeHits.size} results (max 25)")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                    )
                                    for (pc in postcodeHits) {
                                        Text(
                                            text = "${pc.code} · ${pc.localityName} · ${pc.stateCode} · ${pc.type}",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }

                                    selectedState?.let { st ->
                                        StateFieldsBlock(state = st)
                                    }
                                    selectedCity?.let { city ->
                                        CityFieldsBlock(city = city)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Applies [GeoLanguageConfig] and syncs Android per-app locales so [GeoLanguageConfig.AppLanguage]
 * resolves the same locale as the rest of the demo process.
 */
private fun applyAppAndGeoLanguage(geoApp: GeoApplication, config: GeoLanguageConfig) {
    when {
        config.useAppLanguage ->
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        else ->
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(config.fixedLocale.languageTag.replace('_', '-')),
            )
    }
    geoApp.geoKit.setLanguageConfig(config)
    if (config.useAppLanguage) {
        geoApp.geoKit.refreshLanguage()
    }
}

@Composable
private fun DemoSectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 128.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CountryFieldsBlock(country: Country) {
    Text(
        text = "Country",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    val translationsPreview = country.translationsJson.let { js ->
        if (js.length > 280) js.take(280) + "…" else js
    }
    val rows = listOf(
        "id" to country.id.toString(),
        "name" to country.name,
        "iso2" to country.iso2,
        "iso3" to country.iso3,
        "phone" to country.formattedPhoneCode,
        "currency" to country.currency,
        "capital" to country.capital,
        "regionId" to country.regionId.toString(),
        "subregionId" to country.subregionId.toString(),
        "currencyName" to country.currencyName,
        "currencySymbol" to country.currencySymbol,
        "emoji" to country.emoji,
        "emojiUnicode" to country.emojiUnicode,
        "nationality" to country.nationality,
        "numericCode" to country.numericCode,
        "population" to country.population.toString(),
        "gdp" to country.gdp,
        "tld" to country.tld,
        "latitude" to country.latitude.toString(),
        "longitude" to country.longitude.toString(),
        "translationsJson" to translationsPreview,
    )
    for ((k, v) in rows) {
        KeyValueRow(label = k, value = v)
    }
}

@Composable
private fun StateFieldsBlock(state: State) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "State / region",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    val rows = listOf(
        "id" to state.id.toString(),
        "name" to state.name,
        "countryId" to state.countryId.toString(),
        "stateCode" to state.stateCode,
        "timezone" to state.timezone,
        "latitude" to state.latitude.toString(),
        "longitude" to state.longitude.toString(),
        "type" to state.type,
        "iso3166_2" to state.iso3166_2,
    )
    for ((k, v) in rows) {
        KeyValueRow(label = k, value = v)
    }
}

@Composable
private fun CityFieldsBlock(city: City) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "City",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    val rows = listOf(
        "id" to city.id.toString(),
        "name" to city.name,
        "stateId" to city.stateId.toString(),
        "countryId" to city.countryId.toString(),
        "latitude" to city.latitude.toString(),
        "longitude" to city.longitude.toString(),
        "timezone" to city.timezone,
    )
    for ((k, v) in rows) {
        KeyValueRow(label = k, value = v)
    }
}
