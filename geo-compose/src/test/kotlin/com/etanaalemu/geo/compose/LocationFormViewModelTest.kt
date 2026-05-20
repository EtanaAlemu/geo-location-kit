package com.etanaalemu.geo.compose

import com.etanaalemu.geo.core.GeoRepository
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.CountryTimezone
import com.etanaalemu.geo.core.model.Postcode
import com.etanaalemu.geo.core.model.Region
import com.etanaalemu.geo.core.model.State
import com.etanaalemu.geo.core.model.Subregion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocationFormViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectCountry_clearsStateAndCity() = runTest(testDispatcher) {
        val ethiopia = sampleCountry(id = 1, iso2 = "ET")
        val us = sampleCountry(id = 2, iso2 = "US", name = "United States")
        val addis = sampleState(id = 10, countryId = 1, name = "Addis Ababa")
        val repo = FakeGeoRepository(
            countries = listOf(ethiopia, us),
            statesByCountry = mapOf(1 to listOf(addis)),
        )
        val viewModel = LocationFormViewModel(repo)
        subscribeTo(viewModel)

        advanceUntilIdle()
        viewModel.selectCountry(ethiopia)
        viewModel.selectState(addis)
        advanceTimeBy(300)
        advanceUntilIdle()
        viewModel.selectCity(sampleCity(id = 100, stateId = 10))
        advanceUntilIdle()

        viewModel.selectCountry(us)
        advanceUntilIdle()

        assertEquals(us, viewModel.selectedCountry.value)
        assertNull(viewModel.selectedState.value)
        assertNull(viewModel.selectedCity.value)
        assertEquals("", viewModel.citySearchQuery.value)
    }

    @Test
    fun locationSummary_buildsFromSelection() = runTest(testDispatcher) {
        val ethiopia = sampleCountry(id = 1, iso2 = "ET")
        val addis = sampleState(id = 10, countryId = 1, name = "Addis Ababa")
        val city = sampleCity(id = 100, stateId = 10, name = "Bole")
        val repo = FakeGeoRepository(
            countries = listOf(ethiopia),
            statesByCountry = mapOf(1 to listOf(addis)),
            citiesByState = mapOf(10 to listOf(city)),
        )
        val viewModel = LocationFormViewModel(repo)
        subscribeTo(viewModel)

        advanceUntilIdle()
        viewModel.selectCountry(ethiopia)
        advanceUntilIdle()
        viewModel.selectState(addis)
        advanceTimeBy(300)
        advanceUntilIdle()
        viewModel.selectCity(city)
        advanceUntilIdle()

        assertEquals("Ethiopia > Addis Ababa > Bole", viewModel.locationSummary.value)
    }

    private fun sampleCountry(
        id: Int,
        iso2: String,
        name: String = "Ethiopia",
    ) = Country(
        id = id,
        name = name,
        iso2 = iso2,
        iso3 = iso2 + "X",
        formattedPhoneCode = "+1",
        currency = "XXX",
        capital = "Capital",
    )

    private fun sampleState(
        id: Int,
        countryId: Int,
        name: String,
    ) = State(
        id = id,
        name = name,
        countryId = countryId,
        stateCode = "XX",
    )

    private fun sampleCity(
        id: Int,
        stateId: Int,
        name: String = "City",
    ) = City(
        id = id,
        name = name,
        stateId = stateId,
        countryId = 1,
        latitude = 0.0,
        longitude = 0.0,
    )

    private fun TestScope.subscribeTo(viewModel: LocationFormViewModel) {
        backgroundScope.launch { viewModel.countries.collect {} }
        backgroundScope.launch { viewModel.states.collect {} }
        backgroundScope.launch { viewModel.cities.collect {} }
        backgroundScope.launch { viewModel.selectedCountry.collect {} }
        backgroundScope.launch { viewModel.selectedState.collect {} }
        backgroundScope.launch { viewModel.selectedCity.collect {} }
        backgroundScope.launch { viewModel.locationSummary.collect {} }
    }

    private class FakeGeoRepository(
        countries: List<Country>,
        private val statesByCountry: Map<Int, List<State>> = emptyMap(),
        private val citiesByState: Map<Int, List<City>> = emptyMap(),
    ) : GeoRepository {
        private val countriesFlow = MutableStateFlow(countries)
        private val statesFlows = statesByCountry.mapValues { (_, states) -> MutableStateFlow(states) }
        private val citiesFlows = citiesByState.mapValues { (_, cities) -> MutableStateFlow(cities) }

        override fun getCountries(): Flow<List<Country>> = countriesFlow

        override fun getStatesByCountry(countryId: Int): Flow<List<State>> =
            statesFlows[countryId] ?: MutableStateFlow(emptyList())

        override fun searchCitiesInState(stateId: Int, query: String): Flow<List<City>> =
            MutableStateFlow(pageCities(stateId, query, limit = 50, offset = 0))

        override suspend fun searchCitiesInStatePage(
            stateId: Int,
            query: String,
            limit: Int,
            offset: Int,
        ): List<City> = pageCities(stateId, query, limit, offset)

        private fun pageCities(stateId: Int, query: String, limit: Int, offset: Int): List<City> {
            val cities = citiesFlows[stateId]?.value.orEmpty()
            val trimmed = query.trim()
            val filtered = if (trimmed.isEmpty()) {
                cities
            } else {
                cities.filter { it.name.startsWith(trimmed, ignoreCase = true) }
            }
            return filtered.drop(offset).take(limit)
        }

        override suspend fun getCountryById(id: Int): Country? =
            countriesFlow.value.firstOrNull { it.id == id }

        override suspend fun getCountryByIso2(iso2: String): Country? =
            countriesFlow.value.firstOrNull { it.iso2.equals(iso2, ignoreCase = true) }

        override suspend fun findCountriesByPhone(phoneCode: String): List<Country> = emptyList()

        override suspend fun findCountryByPhone(phoneCode: String, iso2Hint: String?): Country? = null

        override fun getRegions(): Flow<List<Region>> = flowOf(emptyList())

        override fun getSubregions(regionId: Int): Flow<List<Subregion>> = flowOf(emptyList())

        override suspend fun getCountryTimezones(countryId: Int): List<CountryTimezone> = emptyList()

        override suspend fun searchPostcodes(countryId: Int, codePrefix: String, limit: Int): List<Postcode> =
            emptyList()
    }
}
