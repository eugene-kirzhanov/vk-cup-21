package by.anegin.vkcup21.features.taxi.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.anegin.vkcup21.core.resources.ResourceProvider
import by.anegin.vkcup21.features.taxi.models.Address
import by.anegin.vkcup21.features.taxi.models.GeoCodeQuery
import by.anegin.vkcup21.features.taxi.models.GeoCodeResult
import by.anegin.vkcup21.features.taxi.models.InfoWindowData
import by.anegin.vkcup21.features.taxi.models.Place
import by.anegin.vkcup21.features.taxi.models.Position
import by.anegin.vkcup21.features.taxi.models.Route
import by.anegin.vkcup21.features.taxi.models.RouteDetails
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.features.taxi.tools.GeoUtil
import by.anegin.vkcup21.features.taxi.tools.LocationProvider
import by.anegin.vkcup21.features.taxi.tools.NearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.tools.OrderManager
import by.anegin.vkcup21.features.taxi.tools.RouteBuilder
import by.anegin.vkcup21.features.taxi.ui.util.InfoWindowGenerator
import by.anegin.vkcup21.taxi.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
    private val geoUtil: GeoUtil,
    private val geoCoder: GeoCoder,
    private val routeBuilder: RouteBuilder,
    private val orderManager: OrderManager,
    private val infoWindowGenerator: InfoWindowGenerator,
    private val nearbyPlacesProvider: NearbyPlacesProvider
) : ViewModel() {

    val myLocation = locationProvider.location
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val reverseGeocodeQuery = MutableSharedFlow<GeoCodeQuery>()
    private val reverseGeocodeResult = reverseGeocodeQuery
        .map(::reverseGeocode)

    var isMapVisible = true

    private val sourcePosition = MutableStateFlow<Position?>(null)
    private val _sourceAddress = MutableStateFlow<Address?>(null)
    val sourceAddress = _sourceAddress
        .asStateFlow()
        .filterNotNull()
        .filter {
            isMapVisible || it.source == Address.Source.USER_SPECIFIED
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val destinationPosition = MutableStateFlow<Position?>(null)
    private val _destinationAddress = MutableStateFlow<Address?>(null)
    val destinationAddress = _destinationAddress
        .asStateFlow()
        .filterNotNull()
        .filter {
            isMapVisible || it.source == Address.Source.USER_SPECIFIED
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val route = combine(sourcePosition, destinationPosition, ::buildRoute)
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

    private val _nearbyPlaces = MutableStateFlow<List<Place>>(emptyList())
    val nearbyPlaces = _nearbyPlaces.asStateFlow()

    private var locationPermissionRequested = false

    init {
        viewModelScope.launch {
            reverseGeocodeResult.collect { result ->
                val address = when (result) {
                    is GeoCodeResult.AddressByLocation -> {
                        Address(
                            latitude = result.query.latitude,
                            longitude = result.query.longitude,
                            type = result.query.addressType,
                            source = result.query.source,
                            title = result.addressTitle ?: "${result.query.latitude}, ${result.query.longitude}"
                        )

                    }
                    is GeoCodeResult.AddressByPlace -> {
                        Address(
                            latitude = result.query.place.latitude,
                            longitude = result.query.place.longitude,
                            type = result.query.addressType,
                            source = result.query.source,
                            title = result.addressTitle ?: "${result.query.place.latitude}, ${result.query.place.longitude}"
                        )
                    }
                }
                when (address.type) {
                    Address.Type.SOURCE -> {
                        sourcePosition.emit(Position(address.latitude, address.longitude))
                        _sourceAddress.emit(address)
                    }
                    Address.Type.DESTINATION -> {
                        destinationPosition.emit(Position(address.latitude, address.longitude))
                        if (address.source != Address.Source.MY_LOCATION) {
                            _destinationAddress.emit(address)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            val places = nearbyPlacesProvider.findNearbyPlaces(20)
            _nearbyPlaces.value = myLocation.value?.let { myLocation ->
                places.sortedBy {
                    geoUtil.getDistance(myLocation.latitude, myLocation.longitude, it.latitude, it.longitude)
                }
            } ?: places
        }
    }

    fun shouldRequestLocationPermission(): Boolean {
        return !locationPermissionRequested
    }

    fun onLocationPermissionRequested() {
        locationPermissionRequested = true
    }

    fun findAddressByPosition(latitude: Double, longitude: Double, source: Address.Source, type: Address.Type) {

        // skip geocoding if we receive coordinates from location provider but user has already selected address manually
        if (source == Address.Source.MY_LOCATION) {
            when (type) {
                Address.Type.SOURCE -> if (_sourceAddress.value?.source == Address.Source.USER_SPECIFIED) return
                Address.Type.DESTINATION -> if (_destinationAddress.value?.source == Address.Source.USER_SPECIFIED) return
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            reverseGeocodeQuery.emit(
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
        destinationPosition.value?.let {
            findAddressByPosition(it.latitude, it.longitude, Address.Source.USER_SPECIFIED, Address.Type.DESTINATION)
        }
    }

    fun onPlaceSelected(place: Place, type: Address.Type) {
        viewModelScope.launch(Dispatchers.Default) {
            reverseGeocodeQuery.emit(
                GeoCodeQuery.AddressByPlace(
                    source = Address.Source.USER_SPECIFIED,
                    place = place,
                    addressType = type
                )
            )
        }
    }

    private suspend fun buildRoute(sourcePosition: Position?, destinationPosition: Position?): Route? {
        return try {
            routeBuilder.buildRoute(sourcePosition, destinationPosition)
        } catch (t: Throwable) {
            if (t !is CancellationException) {
                Timber.w("Error building route: ${t.message}")
            }
            null
        }
    }

    private suspend fun reverseGeocode(query: GeoCodeQuery): GeoCodeResult {
        val (latitude, longitude) = when (query) {
            is GeoCodeQuery.AddressByLocation -> query.latitude to query.longitude
            is GeoCodeQuery.AddressByPlace -> query.place.latitude to query.place.longitude
        }
        val addressTitle = try {
            geoCoder.reverseGeoCode(latitude, longitude)
        } catch (e: Throwable) {
            if (e !is CancellationException) {
                Timber.e("Error geocoding coordinate: ${e.message}")
            }
            null
        }
        return when (query) {
            is GeoCodeQuery.AddressByLocation -> GeoCodeResult.AddressByLocation(query, addressTitle)
            is GeoCodeQuery.AddressByPlace -> GeoCodeResult.AddressByPlace(query, addressTitle ?: query.place.address)
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