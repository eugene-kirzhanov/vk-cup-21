package by.anegin.vkcup21.features.taxi.ui

import androidx.lifecycle.ViewModel
import by.anegin.vkcup21.features.taxi.geo.GeoCodingSource
import by.anegin.vkcup21.features.taxi.location.LocationProvider
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class TaxiOrderingViewModel @Inject constructor(
    locationProvider: LocationProvider,
    private val geoCodingSource: GeoCodingSource
) : ViewModel() {

    val geoCodeResult = locationProvider.location
        .debounce(300)
        .map { LatLng(it.latitude, it.longitude) }
        .map { location ->
            val address = try {
                geoCodingSource.reverseGeoCode(location.latitude, location.longitude)
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    Timber.e(e)
                }
                null
            }
            GeoCodeResult(location, address ?: "${location.latitude}, ${location.longitude}")
        }

    private var locationPermissionRequested = false

    fun shouldRequestLocationPermission(): Boolean {
        return !locationPermissionRequested
    }

    fun onLocationPermissionRequested() {
        locationPermissionRequested = true
    }

    data class GeoCodeResult(
        val query: LatLng,
        val address: String
    )

}