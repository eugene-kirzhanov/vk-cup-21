package by.anegin.vkcup21.features.taxi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.anegin.vkcup21.core.resources.ResourceProvider
import by.anegin.vkcup21.features.taxi.models.Address
import by.anegin.vkcup21.features.taxi.models.AddressMode
import by.anegin.vkcup21.features.taxi.models.GeoCodeQuery
import by.anegin.vkcup21.features.taxi.models.GeoCodeResult
import by.anegin.vkcup21.features.taxi.models.InfoWindowData
import by.anegin.vkcup21.features.taxi.models.RouteDetails
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.features.taxi.tools.LocationProvider
import by.anegin.vkcup21.features.taxi.tools.OrderManager
import by.anegin.vkcup21.features.taxi.tools.RouteBuilder
import by.anegin.vkcup21.features.taxi.ui.util.InfoWindowGenerator
import by.anegin.vkcup21.taxi.R
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TaxiOrderingViewModel @Inject constructor(
    locationProvider: LocationProvider,
    private val resourceProvider: ResourceProvider,
    private val geoCoder: GeoCoder,
    private val routeBuilder: RouteBuilder,
    private val orderManager: OrderManager,
    private val infoWindowGenerator: InfoWindowGenerator
) : ViewModel() {

    val myLocation = locationProvider.location

    private val geocodeQuery = MutableSharedFlow<GeoCodeQuery>(
        replay = 0,
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    private val geoCodeResult = geocodeQuery
        .map { geocode(it) }

    var currentAddressMode = AddressMode.GEOCODING

    private val sourceLatLng = MutableStateFlow<LatLng?>(null)
    private val _sourceAddress = MutableStateFlow<Address?>(null)
    val sourceAddress = _sourceAddress
        .asStateFlow()
        .filterNotNull()
        .filter { currentAddressMode == AddressMode.GEOCODING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val destinationLatLng = MutableStateFlow<LatLng?>(null)
    private val _destinationAddress = MutableStateFlow<Address?>(null)
    val destinationAddress = _destinationAddress
        .asStateFlow()
        .filterNotNull()
        .filter { currentAddressMode == AddressMode.GEOCODING }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val route = combine(sourceLatLng, destinationLatLng, routeBuilder::buildRoute)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val infoWindowData = route
        .map { route ->
            route?.let { orderManager.calculateRouteDetails(it) }
        }
        .map { routeDetails ->
            routeDetails?.let { makeInfoWindowData(it) }
        }

    val infoWindowBitmap = infoWindowData
        .map { data ->
            data?.let {
                data to infoWindowGenerator.generate(data.text)
            }
        }

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
                            Address.Type.SOURCE -> {
                                sourceLatLng.emit(LatLng(address.latitude, address.longitude))
                                _sourceAddress.emit(address)
                            }
                            Address.Type.DESTINATION -> {
                                destinationLatLng.emit(LatLng(address.latitude, address.longitude))
                                if (result.query.source != Address.Source.MY_LOCATION) {
                                    _destinationAddress.emit(address)
                                }
                            }
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

    fun onMarkerDragged(latitude: Double, longitude: Double, source: Address.Source, type: Address.Type) {

        // skip geocoding if we receive coordinates from location provider but user has already selected address manually
        if (source == Address.Source.MY_LOCATION) {
            when (type) {
                Address.Type.SOURCE -> if (_sourceAddress.value?.source == Address.Source.USER_SPECIFIED) return
                Address.Type.DESTINATION -> if (_destinationAddress.value?.source == Address.Source.USER_SPECIFIED) return
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            geocodeQuery.emit(
                GeoCodeQuery.AddressByLocation(
                    source = source,
                    latitude = latitude,
                    longitude = longitude,
                    addressType = type
                )
            )
        }
    }

    fun setDestinationAddressVisible() {
        destinationLatLng.value?.let {
            onMarkerDragged(it.latitude, it.longitude, Address.Source.USER_SPECIFIED, Address.Type.DESTINATION)
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

    private fun makeInfoWindowData(routeDetails: RouteDetails): InfoWindowData {
        val durationString = resourceProvider.getString(R.string.trip_duration, routeDetails.bestVariant.duration)
        val costString = resourceProvider.getString(R.string.trip_cost, routeDetails.bestVariant.cost)
        return InfoWindowData(
            latitude = routeDetails.latitude,
            longitude = routeDetails.longitude,
            text = "$durationString\n$costString"
        )
    }

}