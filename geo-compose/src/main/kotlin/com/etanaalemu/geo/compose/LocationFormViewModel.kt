package com.etanaalemu.geo.compose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.etanaalemu.geo.core.GeoRepository
import com.etanaalemu.geo.core.model.City
import com.etanaalemu.geo.core.model.Country
import com.etanaalemu.geo.core.model.State
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationFormViewModel(
    private val repository: GeoRepository,
) : ViewModel() {
    private val selectedCountryId = MutableStateFlow<Int?>(null)
    private val selectedStateId = MutableStateFlow<Int?>(null)
    private val selectedCityId = MutableStateFlow<Int?>(null)
    private val citySearchQueryState = MutableStateFlow("")

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    private val _cityHasMore = MutableStateFlow(false)
    private val _cityLoadingMore = MutableStateFlow(false)

    val countries: StateFlow<List<Country>> = repository.getCountries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedCountry: StateFlow<Country?> = combine(countries, selectedCountryId) { list, id ->
        id?.let { countryId -> list.firstOrNull { it.id == countryId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val states: StateFlow<List<State>> = selectedCountryId
        .flatMapLatest { countryId ->
            if (countryId == null) flowOf(emptyList())
            else repository.getStatesByCountry(countryId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedState: StateFlow<State?> = combine(states, selectedStateId) { list, id ->
        id?.let { stateId -> list.firstOrNull { it.id == stateId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val citySearchQuery: StateFlow<String> = citySearchQueryState

    val cities: StateFlow<List<City>> = _cities

    val cityHasMore: StateFlow<Boolean> = _cityHasMore

    val cityLoadingMore: StateFlow<Boolean> = _cityLoadingMore

    val selectedCity: StateFlow<City?> = combine(cities, selectedCityId) { list, id ->
        id?.let { cityId -> list.firstOrNull { it.id == cityId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val locationSummary: StateFlow<String> = combine(
        selectedCountry,
        selectedState,
        selectedCity,
    ) { country, state, city ->
        when {
            city != null && state != null && country != null ->
                "${country.name} > ${state.name} > ${city.name}"
            state != null && country != null -> "${country.name} > ${state.name}"
            country != null -> country.name
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    init {
        observeCitySearch()
    }

    @OptIn(FlowPreview::class)
    private fun observeCitySearch() {
        viewModelScope.launch {
            combine(selectedStateId, citySearchQueryState) { stateId, query -> stateId to query }
                .debounce(250)
                .collect { (stateId, query) ->
                    if (stateId == null) {
                        _cities.value = emptyList()
                        _cityHasMore.value = false
                        return@collect
                    }
                    reloadCities(stateId, query)
                }
        }
    }

    private suspend fun reloadCities(stateId: Int, query: String) {
        _cityLoadingMore.value = true
        try {
            val page = repository.searchCitiesInStatePage(
                stateId = stateId,
                query = query,
                limit = CITY_PAGE_SIZE,
                offset = 0,
            )
            _cities.value = page
            _cityHasMore.value = page.size >= CITY_PAGE_SIZE
        } finally {
            _cityLoadingMore.value = false
        }
    }

    fun loadMoreCities() {
        val stateId = selectedStateId.value ?: return
        if (_cityLoadingMore.value || !_cityHasMore.value) return
        val query = citySearchQueryState.value
        viewModelScope.launch {
            _cityLoadingMore.value = true
            try {
                val page = repository.searchCitiesInStatePage(
                    stateId = stateId,
                    query = query,
                    limit = CITY_PAGE_SIZE,
                    offset = _cities.value.size,
                )
                _cities.value = _cities.value + page
                _cityHasMore.value = page.size >= CITY_PAGE_SIZE
            } finally {
                _cityLoadingMore.value = false
            }
        }
    }

    fun selectCountry(country: Country) {
        selectedCountryId.value = country.id
        selectedStateId.value = null
        selectedCityId.value = null
        citySearchQueryState.value = ""
        _cities.value = emptyList()
        _cityHasMore.value = false
    }

    fun selectState(state: State) {
        selectedStateId.value = state.id
        selectedCityId.value = null
        citySearchQueryState.value = ""
    }

    fun selectCity(city: City) {
        selectedCityId.value = city.id
    }

    fun updateCitySearchQuery(query: String) {
        citySearchQueryState.value = query
    }

    companion object {
        const val CITY_PAGE_SIZE = 50
    }
}
