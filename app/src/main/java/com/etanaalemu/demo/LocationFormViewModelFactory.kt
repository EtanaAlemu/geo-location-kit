package com.etanaalemu.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.etanaalemu.geo.compose.LocationFormViewModel
import com.etanaalemu.geo.core.GeoRepository

class LocationFormViewModelFactory(
    private val repository: GeoRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationFormViewModel::class.java)) {
            return LocationFormViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
