package by.anegin.vkcup21.features.taxi.ui.order

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.anegin.vkcup21.core.resources.ResourceProvider
import by.anegin.vkcup21.features.taxi.data.GeoCoder
import by.anegin.vkcup21.features.taxi.data.GeoUtil
import by.anegin.vkcup21.features.taxi.data.LocationProvider
import by.anegin.vkcup21.features.taxi.data.NearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.data.OrderManager
import by.anegin.vkcup21.features.taxi.data.RouteBuilder
import by.anegin.vkcup21.features.taxi.data.models.Address
import by.anegin.vkcup21.features.taxi.data.models.GeoCodeQuery
import by.anegin.vkcup21.features.taxi.data.models.GeoCodeResult
import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.features.taxi.data.models.Route
import by.anegin.vkcup21.features.taxi.data.models.RouteDetails
import by.anegin.vkcup21.features.taxi.ui.order.models.InputQuery
import by.anegin.vkcup21.features.taxi.ui.order.models.RouteVariant
import by.anegin.vkcup21.features.taxi.ui.order.models.toRouteVariant
import by.anegin.vkcup21.features.taxi.ui.order.util.DateTimeUtil
import by.anegin.vkcup21.features.taxi.ui.order.util.InfoWindowGenerator
import by.anegin.vkcup21.taxi.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
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
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal class TaxiOrderingViewModel @Inject constructor(
    locationProvider: LocationProvider,
    private val resources: ResourceProvider,
    private val geoUtil: GeoUtil,
    private val geoCoder: GeoCoder,
    private val routeBuilder: RouteBuilder,
    private val orderManager: OrderManager,
    private val infoWindowGenerator: InfoWindowGenerator,
    private val nearbyPlacesProvider: NearbyPlacesProvider
) : ViewModel() {

    private var locationPermissionRequested = false

    private val _isLocationPermissionGranted = MutableStateFlow(false)
    val isLocationPermissionGranted = _isLocationPermissionGranted.asStateFlow()

    private val _isMyLocationOnScreen = MutableStateFlow(false)

    val isMyLocationButtonVisible = combine(_isLocationPermissionGranted, _isMyLocationOnScreen) { isPermissionGranted, isMyLocationOnScreen ->
        isPermissionGranted && !isMyLocationOnScreen
    }

    val myLocation = locationProvider.location
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val reverseGeocodeQuery = MutableSharedFlow<GeoCodeQuery>(replay = 0, extraBufferCapacity = 1000, onBufferOverflow = BufferOverflow.SUSPEND)
    private val reverseGeocodeResult = reverseGeocodeQuery
        .map(::reverseGeocode)

    var isMapVisible = true

    private val sourcePosition = MutableStateFlow<Position?>(null)
    private val destinationPosition = MutableStateFlow<Position?>(null)

    private val _sourceAddress = MutableStateFlow<Address?>(null)
    val sourceAddress = _sourceAddress
        .filterNotNull()
        .filter { isMapVisible || it.source == Address.Source.USER_SPECIFIED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _destinationAddress = MutableStateFlow<Address?>(null)
    val destinationAddress = _destinationAddress
        .filterNotNull()
        .filter { isMapVisible || it.source == Address.Source.USER_SPECIFIED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val route = combine(sourcePosition, destinationPosition, ::buildRoute)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val routeDetails = route
        .map(::calculateRouteDetails)

    val routeInfoWindow = routeDetails
        .map(::makeRouteInfoWindow)

    private val _selectedRouteVariant = MutableStateFlow<RouteVariant?>(null)
    val selectedRouteVariant = _selectedRouteVariant.asStateFlow()

    val routeVariants = combine(routeDetails, _selectedRouteVariant, ::makeRouteVariantsList)

    private val nearbyPlacesRequestedOnce = AtomicBoolean(false)
    private val nearbyPlaces = combine(
        isLocationPermissionGranted
            // await until permission is granted
            .filter { it },
        locationProvider.location
            // await and allow only first request
            .filter { !nearbyPlacesRequestedOnce.getAndSet(true) },
        ::maybeRequestNearbyPlaces
    )

    private val searchQuery = MutableStateFlow<InputQuery?>(null)

    val places = combine(
        searchQuery.debounce(300),
        nearbyPlaces,
        ::makePlacesList
    )

    private val _isInRouteDetailsMode = MutableStateFlow(false)
    val isInRouteDetailsMode = _isInRouteDetailsMode.asStateFlow()

    init {
        viewModelScope.launch {
            reverseGeocodeResult
                .collect { result ->
                    val address = result.toAddress()
                    when (address.type) {
                        Address.Type.SOURCE -> {
                            sourcePosition.emit(address.position)
                            _sourceAddress.emit(address)
                        }
                        Address.Type.DESTINATION -> {
                            destinationPosition.emit(address.position)
                            if (address.source != Address.Source.MY_LOCATION) {
                                _destinationAddress.emit(address)
                            }
                        }
                    }
                }
        }
        viewModelScope.launch {
            routeDetails
                .collect { routeDetails ->
                    routeDetails?.let {
                        if (_selectedRouteVariant.value == null) {
                            // select middle variant by default
                            _selectedRouteVariant.value =
                                it.variants.getOrNull(it.variants.size / 2)?.toRouteVariant(resources, true)
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

    fun onLocationPermissionGranted(isGranted: Boolean) {
        _isLocationPermissionGranted.value = isGranted
    }

    fun setMyLocationVisibleOnScreen(isMyLocationVisibleOnScreen: Boolean) {
        _isMyLocationOnScreen.value = isMyLocationVisibleOnScreen
    }

    fun findPlacesByText(text: String, addressType: Address.Type) {
        searchQuery.value = InputQuery(text.trim(), addressType)
    }

    fun findAddressByPosition(position: Position, source: Address.Source, type: Address.Type) {
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
                    position = position,
                    addressType = type
                )
            )
        }
    }

    fun setDestinationAddressVisible() {
        destinationPosition.value?.let {
            findAddressByPosition(it, Address.Source.USER_SPECIFIED, Address.Type.DESTINATION)
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

    fun toggleRouteDetailsMode() {
        _isInRouteDetailsMode.value = !_isInRouteDetailsMode.value
    }

    fun onRouteVariantSelected(variant: RouteVariant?) {
        this._selectedRouteVariant.value = variant
    }

    private suspend fun maybeRequestNearbyPlaces(isLocationPermissionGranted: Boolean, myLocation: Position?): List<Place> {
        return if (isLocationPermissionGranted) {
            val places = nearbyPlacesProvider.findNearbyPlaces(20)
            myLocation?.let { location ->
                places.sortedBy {
                    geoUtil.getDistance(location, it.position)
                }
            } ?: places
        } else {
            emptyList()
        }
    }

    private suspend fun makePlacesList(query: InputQuery?, nearbyPlaces: List<Place>): List<Place> {
        return query?.let {
            if (it.text.isNotBlank()) {
                try {
                    geoCoder.geocode(query.text)
                } catch (t: Throwable) {
                    Timber.w("Error searching places: ${t.message}")
                    emptyList()
                }
            } else {
                nearbyPlaces
            }
        } ?: nearbyPlaces
    }

    private suspend fun reverseGeocode(query: GeoCodeQuery): GeoCodeResult {
        val position = when (query) {
            is GeoCodeQuery.AddressByLocation -> query.position
            is GeoCodeQuery.AddressByPlace -> query.place.position
        }
        val addressTitle = try {
            geoCoder.reverseGeoCode(position)
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

    private suspend fun buildRoute(sourcePosition: Position?, destinationPosition: Position?): RouteResult? {
        return if (sourcePosition != null && destinationPosition != null) {
            val route = try {
                routeBuilder.buildRoute(sourcePosition, destinationPosition)
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    Timber.w("Error building route: ${t.message}")
                }
                null
            }
            RouteResult(sourcePosition, destinationPosition, route)
        } else {
            null
        }
    }

    private suspend fun calculateRouteDetails(routeResult: RouteResult?): RouteDetails? {
        return routeResult?.result?.let {
            orderManager.calculateRouteDetails(it)
        }
    }

    private suspend fun makeRouteInfoWindow(routeDetails: RouteDetails?): Pair<Position, Bitmap>? {
        return routeDetails?.let {
            val duration = DateTimeUtil.minutesToHumanReadableTime(resources, routeDetails.bestVariant.duration)
            val durationString = resources.getString(R.string.trip_duration, duration)

            val cost = resources.getString(R.string.common_cost_in_rubles, routeDetails.bestVariant.cost)
            val costString = resources.getString(R.string.trip_cost, cost)

            val infoWindowText = "$durationString\n$costString"

            val infoWindowBitmap = infoWindowGenerator.generate(infoWindowText)

            routeDetails.route.destination to infoWindowBitmap
        }
    }

    private fun makeRouteVariantsList(routeDetails: RouteDetails?, selectedVariant: RouteVariant?): List<RouteVariant> {
        val selectedVariantId = selectedVariant?.id
            ?: routeDetails?.variants?.getOrNull(routeDetails.variants.size / 2)?.id
        return routeDetails?.variants
            ?.map {
                it.toRouteVariant(resources, isSelected = it.id == selectedVariantId)
            }
            ?: emptyList()
    }

    data class RouteResult(
        val source: Position,
        val destination: Position,
        val result: Route?
    )

}