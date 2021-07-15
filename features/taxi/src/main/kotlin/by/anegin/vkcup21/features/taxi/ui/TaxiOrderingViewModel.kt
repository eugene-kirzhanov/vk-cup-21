package by.anegin.vkcup21.features.taxi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.anegin.vkcup21.features.taxi.models.Address
import by.anegin.vkcup21.features.taxi.models.AddressMode
import by.anegin.vkcup21.features.taxi.models.GeoCodeQuery
import by.anegin.vkcup21.features.taxi.models.GeoCodeResult
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.features.taxi.tools.LocationProvider
import by.anegin.vkcup21.features.taxi.tools.RouteBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TaxiOrderingViewModel @Inject constructor(
    locationProvider: LocationProvider,
    private val geoCoder: GeoCoder,
    private val routeBuilder: RouteBuilder
) : ViewModel() {

    val myLocation = locationProvider.location

    private val geocodeQuery = MutableSharedFlow<GeoCodeQuery>()
    private val geoCodeResult = geocodeQuery
        .debounce(300)
        .map { query ->
            geocode(query)
        }

    var currentAddressMode = AddressMode.GEOCODING

    private val _currentAddressType = MutableStateFlow(Address.Type.DESTINATION)
    val currentAddressType = _currentAddressType.asStateFlow()

    private val _sourceAddress = MutableStateFlow<Address?>(null)
    val sourceAddress = _sourceAddress
        .asStateFlow()
        .filterNotNull()
        .filter { currentAddressMode == AddressMode.GEOCODING }

    private val _destinationAddress = MutableStateFlow<Address?>(null)
    val destinationAddress = _destinationAddress
        .asStateFlow()
        .filterNotNull()
        .filter { currentAddressMode == AddressMode.GEOCODING }

    val route = combine(sourceAddress, destinationAddress, routeBuilder::buildRoute)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var locationPermissionRequested = false

    init {
        viewModelScope.launch {
            geoCodeResult.collect { result ->
                when (result) {
                    is GeoCodeResult.AddressByLocation -> {
                        val address = Address(
                            latitude = result.query.latitude,
                            longitude = result.query.longitude,
                            type = result.query.addressType,
                            source = result.query.source,
                            title = result.address ?: "${result.query.latitude}, ${result.query.longitude}"
                        )
                        when (result.query.addressType) {
                            Address.Type.SOURCE -> _sourceAddress.emit(address)
                            Address.Type.DESTINATION -> _destinationAddress.emit(address)
                        }
                    }
                }
            }
        }
    }

    fun shouldRequestLocationPermission(): Boolean {
        return !locationPermissionRequested
    }

    fun onLocationPermissionRequested() {
        locationPermissionRequested = true
    }

    fun selectAddressType(type: Address.Type) {
        _currentAddressType.value = type
    }

    fun onMarkerDragged(latitude: Double, longitude: Double, source: Address.Source) {

        // skip geocoding if we receive coordinates from location provider but user has already selected address manually
        if (source == Address.Source.MY_LOCATION) {
            when (_currentAddressType.value) {
                Address.Type.SOURCE -> if (_sourceAddress.value?.source == Address.Source.USER_SPECIFIED) return
                Address.Type.DESTINATION -> if (_destinationAddress.value?.source == Address.Source.USER_SPECIFIED) return
            }
        }

        viewModelScope.launch {
            geocodeQuery.emit(
                GeoCodeQuery.AddressByLocation(
                    source = source,
                    latitude = latitude,
                    longitude = longitude,
                    addressType = _currentAddressType.value
                )
            )
        }
    }

    private suspend fun geocode(query: GeoCodeQuery): GeoCodeResult {
        return when (query) {
            is GeoCodeQuery.AddressByLocation -> {
                val address = try {
                    geoCoder.reverseGeoCode(query.latitude, query.longitude)
                } catch (e: Throwable) {
                    if (e !is CancellationException) {
                        Timber.e(e)
                    }
                    null
                }
                GeoCodeResult.AddressByLocation(query, address)
            }
        }
    }

}