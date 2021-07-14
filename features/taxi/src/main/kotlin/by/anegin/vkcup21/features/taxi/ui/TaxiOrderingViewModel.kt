package by.anegin.vkcup21.features.taxi.ui

import androidx.lifecycle.ViewModel
import by.anegin.vkcup21.features.taxi.location.LocationProvider
import javax.inject.Inject

class TaxiOrderingViewModel @Inject constructor(
    locationProvider: LocationProvider
) : ViewModel() {

    val myLocation = locationProvider.location

    private var locationPermissionRequested = false

    fun shouldRequestLocationPermission(): Boolean {
        return !locationPermissionRequested
    }

    fun onLocationPermissionRequested() {
        locationPermissionRequested = true
    }

}