package by.anegin.vkcup21.features.taxi.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.core.util.dp
import by.anegin.vkcup21.core.util.hideKeyboard
import by.anegin.vkcup21.core.util.isDestroyed
import by.anegin.vkcup21.core.util.observe
import by.anegin.vkcup21.core.util.showKeyboard
import by.anegin.vkcup21.di.taxi.DaggerTaxiComponent
import by.anegin.vkcup21.di.taxi.TaxiModuleDependencies
import by.anegin.vkcup21.features.taxi.models.Address
import by.anegin.vkcup21.features.taxi.models.InfoWindowData
import by.anegin.vkcup21.features.taxi.ui.util.VerticalSpacingItemDecoration
import by.anegin.vkcup21.taxi.R
import by.anegin.vkcup21.taxi.databinding.FragmentTaxiOrderingBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class TaxiOrderingFragment : Fragment(R.layout.fragment_taxi_ordering) {

    companion object {
        private const val DEFAULT_ZOOM = 15.0

        private const val IMAGE_SOURCE_ADDRESS_PIN = "image-source-address-pin"
        private const val IMAGE_DESTINATION_ADDRESS_PIN = "image-destination-address-pin"
        private const val IMAGE_INFO_WINDOW = "image-info-window"

        private const val ROUTE_LAYER_ID = "route-layer-id"
        private const val ROUTE_SOURCE_ID = "route-source-id"
    }

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<TaxiOrderingViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentTaxiOrderingBinding::class.java)

    private var map: MapboxMap? = null
    private var symbolManager: SymbolManager? = null

    private var systemBarInsets = 0 to 0 // top, bottom

    private var sourceAddressMarker: Symbol? = null
    private var destinationAddressMarker: Symbol? = null
    private var isSourceAddressMarkerDragged = false
    private var isDestinationAddressMarkerDragged = false

    private var infoWindow: Symbol? = null

    private val isPermissionGranted = MutableStateFlow(false)
    private val isMyLocationOnScreen = MutableStateFlow(false)
    private val isMyLocationButtonVisible = combine(isPermissionGranted, isMyLocationOnScreen) { isPermissionGranted, isMyLocationOnScreen ->
        isPermissionGranted && !isMyLocationOnScreen
    }

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var bottomSheetPreviousState = BottomSheetBehavior.STATE_COLLAPSED

    private var pendingAddressTypeForFocus: Address.Type? = null

    private var awaitingRoute = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerTaxiComponent.builder()
            .context(context.applicationContext)
            .taxiModuleDependencies(EntryPointAccessors.fromApplication(context, TaxiModuleDependencies::class.java))
            .build()
            .injectTaxiOrderingFragment(this)

        initMap(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        appUiController.setupSystemBars(statusBar = SystemBar.Transparent, navigationBar = SystemBar.Default)

        view.setOnApplyWindowInsetsListener { _, insets ->
            val systemBars = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            systemBarInsets = systemBars.top to systemBars.bottom

            binding.bottomSheetAddressess.textAddressesTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding.bottomSheetAddressess.buttonCloseAddresses.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            WindowInsetsCompat.CONSUMED.toWindowInsets()
        }

        val bottomSheetLayoutParams = binding.bottomSheetAddressess.root.layoutParams as? CoordinatorLayout.LayoutParams
        bottomSheetBehavior = bottomSheetLayoutParams?.behavior as? BottomSheetBehavior<*>
        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val treshold = 0.5f
                val offsetWithTreshold = if (slideOffset > treshold) (slideOffset - treshold) / (1f - treshold) else 0f

                val addressesContainerTopMargin =
                    (slideOffset * (resources.getDimension(R.dimen.addresses_container_expanded_top_margin) + systemBarInsets.first)).toInt()
                val addressesContainerHorizontalMargins =
                    (slideOffset * resources.getDimension(R.dimen.addresses_container_expanded_horizontal_margins)).toInt()
                val addressesContainerElevation =
                    offsetWithTreshold * resources.getDimension(R.dimen.addresses_container_expanded_elevation)
                binding.bottomSheetAddressess.addressesContainer.apply {
                    elevation = addressesContainerElevation
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = addressesContainerTopMargin
                        leftMargin = addressesContainerHorizontalMargins
                        rightMargin = addressesContainerHorizontalMargins
                    }
                }

                binding.bottomSheetAddressess.textAddressesTitle.apply {
                    alpha = offsetWithTreshold
                    scaleX = offsetWithTreshold
                    scaleY = offsetWithTreshold
                }
                binding.bottomSheetAddressess.buttonCloseAddresses.apply {
                    alpha = offsetWithTreshold
                    scaleX = offsetWithTreshold
                    scaleY = offsetWithTreshold
                }

                val doneButtonState = if (viewModel.route.value != null) 1f - slideOffset else 0f
                binding.bottomSheetAddressess.buttonDone.apply {
                    alpha = doneButtonState
                    scaleX = doneButtonState
                    scaleY = doneButtonState
                }

                binding.bottomSheetAddressess.recyclerViewPlaces.alpha = offsetWithTreshold

                updateMapLogoAndAttributionPosition()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    viewModel.isMapVisible = true
                    setInputAllowed(false)
                    bottomSheetPreviousState = newState
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED && bottomSheetPreviousState != BottomSheetBehavior.STATE_EXPANDED) {
                    viewModel.isMapVisible = false
                    setInputAllowed(true)
                    bottomSheetPreviousState = newState
                }
            }
        })

        binding.bottomSheetAddressess.editSourceAddress.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.bottomSheetAddressess.textAddressesTitle.setText(R.string.address_source_hint)
            }
        }
        binding.bottomSheetAddressess.editDestinationAddress.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.bottomSheetAddressess.textAddressesTitle.setText(R.string.address_destination_hint)
            }
        }

        binding.bottomSheetAddressess.sourceAddressOverlay.setOnClickListener {
            pendingAddressTypeForFocus = Address.Type.SOURCE
            binding.bottomSheetAddressess.textAddressesTitle.setText(R.string.address_source_hint)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.bottomSheetAddressess.destinationAddressOverlay.setOnClickListener {
            pendingAddressTypeForFocus = Address.Type.DESTINATION
            binding.bottomSheetAddressess.textAddressesTitle.setText(R.string.address_destination_hint)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.bottomSheetAddressess.buttonCloseAddresses.setOnClickListener {
            setInputAllowed(false)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                        setInputAllowed(false)
                        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        appNavigator.navigateUp()
                    }
                }
            }
        )

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.addOnCameraWillChangeListener {
            updateMyLocationButtonVisibility()
        }
        binding.mapView.addOnCameraIsChangingListener {
            updateMyLocationButtonVisibility()
        }
        binding.mapView.getMapAsync { map ->
            if (!isDestroyed()) {
                onMapReady(map)
            }
        }

        binding.buttonMyLocation.setOnClickListener {
            moveCameraToMyLocation()
        }

        isPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            updateMyLocationState(isGranted)
        }
        isMyLocationButtonVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible && !binding.buttonMyLocation.isOrWillBeShown) {
                binding.buttonMyLocation.show()
            } else if (!isVisible && !binding.buttonMyLocation.isOrWillBeHidden) {
                binding.buttonMyLocation.hide()
            }
        }

        viewModel.sourceAddress.observe(viewLifecycleOwner) { address ->
            val title = address?.title.orEmpty()
            binding.bottomSheetAddressess.editSourceAddress.apply {
                setText(title)
                setSelection(title.length)
            }
            address?.let {
                maybeUpdateSourceAddressMarkerByMyLocation(it.latitude, it.longitude)
                if (it.source == Address.Source.USER_SPECIFIED) {
                    isSourceAddressMarkerDragged = true
                }
            }
        }
        viewModel.destinationAddress.observe(viewLifecycleOwner) { address ->
            val title = address?.title.orEmpty()
            binding.bottomSheetAddressess.editDestinationAddress.apply {
                setText(title)
                setSelection(title.length)
            }
            address?.let {
                maybeUpdateDestinationAddressMarkerByMyLocation(it.latitude, it.longitude)
                if (it.source == Address.Source.USER_SPECIFIED) {
                    isDestinationAddressMarkerDragged = true
                }
            }
        }

        viewModel.myLocation.observe(viewLifecycleOwner) { myLocation ->
            if (map != null && myLocation != null) {
                if (!isDestinationAddressMarkerDragged) {
                    maybeUpdateDestinationAddressMarkerByMyLocation(myLocation.latitude, myLocation.longitude)
                }
                if (!isSourceAddressMarkerDragged) {
                    maybeUpdateSourceAddressMarkerByMyLocation(myLocation.latitude, myLocation.longitude)
                }
            }
        }

        viewModel.route.observe(viewLifecycleOwner) { route ->
            updateDoneButtonState(isRouteAvailable = route != null)

            map?.getStyle { style ->
                val routeGeoJson: GeoJsonSource? = style.getSourceAs(ROUTE_SOURCE_ID)
                route?.direction?.geometry()?.let { geometry ->
                    routeGeoJson?.setGeoJson(LineString.fromPolyline(geometry, PRECISION_6))
                } ?: run {
                    routeGeoJson?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
                }
            }

            if (route != null && awaitingRoute) {
                awaitingRoute = false
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        viewModel.infoWindowBitmap.observe(viewLifecycleOwner, observer = ::showInfoWindow)

        // === Places ===

        val placesAdapter = PlacesAdapter { place ->
            val currentAddressType = if (binding.bottomSheetAddressess.editSourceAddress.isFocused) {
                Address.Type.SOURCE
            } else {
                Address.Type.DESTINATION
            }
            awaitingRoute = true
            viewModel.onPlaceSelected(place, currentAddressType)
        }
        binding.bottomSheetAddressess.recyclerViewPlaces.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = placesAdapter
            addItemDecoration(
                VerticalSpacingItemDecoration(
                    topSpacing = 18.dp,
                    bottomSpacing = 18.dp + systemBarInsets.second
                )
            )
        }

        viewModel.nearbyPlaces.observe(viewLifecycleOwner) { places ->
            placesAdapter.submitList(places)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        checkForLocationPermission()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        binding.mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onDestroyView() {
        bottomSheetBehavior = null
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    // === Location permissions ===

    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isPermissionGranted.value = granted
    }

    private fun checkForLocationPermission() {
        isPermissionGranted.value =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!isPermissionGranted.value && viewModel.shouldRequestLocationPermission()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.system_location_permission_rationale_title)
                    .setMessage(R.string.system_location_permission_rationale_message)
                    .setPositiveButton(R.string.common_continue) { _, _ ->
                        requestLocationPermission()
                    }
                    .show()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        viewModel.onLocationPermissionRequested()
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // === Map ===

    private fun initMap(context: Context) {
        Mapbox.getInstance(context, getString(R.string.mapbox_access_token))
        Mapbox.getTelemetry()?.setDebugLoggingEnabled(true)
    }

    private fun onMapReady(map: MapboxMap) {
        this.map = map

        updateMapLogoAndAttributionPosition()

        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isTiltGesturesEnabled = false

        map.setStyle(Style.MAPBOX_STREETS) { loadedStyle ->

            val locationComponentOptions = LocationComponentOptions.builder(requireContext())
                .trackingGesturesManagement(true)
                .elevation(0f)
                .foregroundStaleTintColor(ContextCompat.getColor(requireContext(), R.color.my_location_marker_foreground_color))
                .foregroundTintColor(ContextCompat.getColor(requireContext(), R.color.my_location_marker_foreground_color))
                .accuracyColor(ContextCompat.getColor(requireContext(), R.color.my_location_marker_accuracy))
                .accuracyAlpha(1f)
                .build()
            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), loadedStyle)
                .locationComponentOptions(locationComponentOptions)
                .build()
            map.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)
                renderMode = RenderMode.COMPASS
            }

            val localizationPlugin = LocalizationPlugin(binding.mapView, map, loadedStyle)
            localizationPlugin.matchMapLanguageWithDeviceDefault()

            updateMyLocationState(isPermissionGranted.value)

            // === setup route layer ===

            val routeLayer = LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
            routeLayer.setProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(ContextCompat.getColor(requireContext(), R.color.route_line))
            )
            loadedStyle.addSource(GeoJsonSource(routeLayer.sourceId))
            loadedStyle.addLayer(routeLayer)

            // === setup markers layer ===

            val sourceAddressMarkerDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.pin_source_address)
                ?: throw RuntimeException("Invalid address marker drawable")
            loadedStyle.addImage(IMAGE_SOURCE_ADDRESS_PIN, sourceAddressMarkerDrawable)

            val destinationAddressMarkerDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.pin_destination_address)
                ?: throw RuntimeException("Invalid destination address marker drawable")
            loadedStyle.addImage(IMAGE_DESTINATION_ADDRESS_PIN, destinationAddressMarkerDrawable)

            symbolManager = SymbolManager(binding.mapView, map, loadedStyle).apply {
                iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT
                iconAllowOverlap = true
                textAllowOverlap = true
                addDragListener(object : OnSymbolDragListener {
                    override fun onAnnotationDragStarted(annotation: Symbol) {
                        if (annotation.iconImage == IMAGE_SOURCE_ADDRESS_PIN) {
                            isSourceAddressMarkerDragged = true
                        } else if (annotation.iconImage == IMAGE_DESTINATION_ADDRESS_PIN) {
                            isDestinationAddressMarkerDragged = true
                            removeInfoWindow()
                        }
                    }

                    override fun onAnnotationDrag(annotation: Symbol?) {}

                    override fun onAnnotationDragFinished(annotation: Symbol) {
                        val addressType = when (annotation.iconImage) {
                            IMAGE_SOURCE_ADDRESS_PIN -> Address.Type.SOURCE
                            IMAGE_DESTINATION_ADDRESS_PIN -> Address.Type.DESTINATION
                            else -> null
                        }
                        addressType?.let {
                            viewModel.findAddressByPosition(annotation.latLng.latitude, annotation.latLng.longitude, Address.Source.USER_SPECIFIED, it)

                            if (addressType == Address.Type.SOURCE && !isDestinationAddressMarkerDragged && viewModel.destinationAddress.value == null) {
                                viewModel.setDestinationAddressVisible()
                            }
                        }
                    }
                })
            }

        }
    }

    private fun updateMyLocationState(isLocationPermissionGranted: Boolean) {
        map?.locationComponent?.apply {
            if (isLocationComponentActivated) {
                @SuppressLint("MissingPermission")
                if (isLocationPermissionGranted && !isLocationComponentEnabled) {
                    isLocationComponentEnabled = true
                    cameraMode = CameraMode.TRACKING
                    zoomWhileTracking(DEFAULT_ZOOM)
                } else if (!isLocationPermissionGranted && isLocationComponentEnabled) {
                    isLocationComponentEnabled = false
                    cameraMode = CameraMode.NONE
                }
            }
        }
    }

    private fun moveCameraToMyLocation() {
        map?.locationComponent?.apply {
            lastKnownLocation?.let { location ->
                map?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(location.latitude, location.longitude))
                            .zoom(DEFAULT_ZOOM)
                            .bearing(0.0)
                            .build()
                    ),
                    object : MapboxMap.CancelableCallback {
                        override fun onFinish() {
                            if (isLocationComponentActivated && isLocationComponentEnabled) {
                                cameraMode = CameraMode.TRACKING
                            }
                        }

                        override fun onCancel() {}
                    }
                )
            }
        }
    }

    private fun updateMyLocationButtonVisibility() {
        isMyLocationOnScreen.value = map?.run {
            locationComponent.lastKnownLocation?.let {
                projection.visibleRegion.latLngBounds.contains(LatLng(it.latitude, it.longitude))
            } ?: true
        } ?: true
    }

    private fun updateMapLogoAndAttributionPosition() {
        map?.uiSettings?.apply {
            val defaultLogoMargin = resources.getDimension(com.mapbox.mapboxsdk.R.dimen.mapbox_four_dp)
            var bottomMargin = (binding.root.height - binding.bottomSheetAddressess.root.y + defaultLogoMargin).toInt()
            if (bottomMargin < systemBarInsets.second) bottomMargin = (systemBarInsets.second + defaultLogoMargin).toInt()
            setLogoMargins(logoMarginLeft, logoMarginTop, logoMarginRight, bottomMargin)
            setAttributionMargins(attributionMarginLeft, attributionMarginTop, attributionMarginRight, bottomMargin)
        }
    }

    private fun maybeUpdateSourceAddressMarkerByMyLocation(latitude: Double, longitude: Double) {
        var updated = false
        this.sourceAddressMarker
            ?.apply {
                if (this.latLng.latitude != latitude || this.latLng.longitude != longitude) {
                    this.latLng = LatLng(latitude, longitude)
                    symbolManager?.update(this)
                    updated = true
                }
            }
            ?: symbolManager?.create(
                SymbolOptions()
                    .withLatLng(LatLng(latitude, longitude))
                    .withIconImage(IMAGE_SOURCE_ADDRESS_PIN)
                    .withIconAnchor(Property.ICON_ANCHOR_CENTER)
                    .withDraggable(true)
            ).also {
                this.sourceAddressMarker = it
                updated = true
            }
        if (updated) {
            viewModel.findAddressByPosition(latitude, longitude, Address.Source.MY_LOCATION, Address.Type.SOURCE)
        }
    }

    private fun maybeUpdateDestinationAddressMarkerByMyLocation(latitude: Double, longitude: Double) {
        var updated = false
        this.destinationAddressMarker
            ?.apply {
                if (this.latLng.latitude != latitude || this.latLng.longitude != longitude) {
                    this.latLng = LatLng(latitude, longitude)
                    symbolManager?.update(this)
                    updated = true
                }
            }
            ?: symbolManager?.create(
                SymbolOptions()
                    .withLatLng(LatLng(latitude, longitude))
                    .withIconImage(IMAGE_DESTINATION_ADDRESS_PIN)
                    .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                    .withDraggable(true)
            ).also {
                this.destinationAddressMarker = it
                updated = true
            }
        if (updated) {
            viewModel.findAddressByPosition(latitude, longitude, Address.Source.MY_LOCATION, Address.Type.DESTINATION)
        }
    }

    private fun setInputAllowed(inputAllowed: Boolean) {
        binding.bottomSheetAddressess.apply {
            editSourceAddress.isEnabled = inputAllowed
            editDestinationAddress.isEnabled = inputAllowed
            sourceAddressOverlay.isVisible = !inputAllowed
            destinationAddressOverlay.isVisible = !inputAllowed
        }
        if (inputAllowed) {
            // determine field to be focused
            val addressTypeForFocus = pendingAddressTypeForFocus
                ?.also { pendingAddressTypeForFocus = null }
                ?: run {
                    when {
                        binding.bottomSheetAddressess.editSourceAddress.text?.isEmpty() == true -> Address.Type.SOURCE
                        binding.bottomSheetAddressess.editDestinationAddress.text?.isEmpty() == true -> Address.Type.DESTINATION
                        else -> Address.Type.DESTINATION
                    }
                }
            when (addressTypeForFocus) {
                Address.Type.SOURCE -> binding.bottomSheetAddressess.editSourceAddress.requestFocus()
                Address.Type.DESTINATION -> binding.bottomSheetAddressess.editDestinationAddress.requestFocus()
            }

            showKeyboard()
        } else {

            // restore text in input fields
            viewModel.sourceAddress.value?.let { address ->
                binding.bottomSheetAddressess.editSourceAddress.apply {
                    setText(address.title)
                    setSelection(address.title.length)
                }
            }
            viewModel.destinationAddress.value?.let { address ->
                binding.bottomSheetAddressess.editDestinationAddress.apply {
                    setText(address.title)
                    setSelection(address.title.length)
                }
            }

            activity?.currentFocus?.clearFocus()
            hideKeyboard()
        }
    }

    private fun updateDoneButtonState(isRouteAvailable: Boolean) {
        if (isRouteAvailable) {
            val peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_with_button)
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior?.setPeekHeight(peekHeight, true)
                binding.bottomSheetAddressess.buttonDone.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .start()
            } else {
                bottomSheetBehavior?.peekHeight = peekHeight
            }
        } else {
            val peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)
            if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior?.setPeekHeight(peekHeight, true)
                binding.bottomSheetAddressess.buttonDone.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .start()
            } else {
                bottomSheetBehavior?.peekHeight = peekHeight
            }
        }
    }

    private fun showInfoWindow(data: Pair<InfoWindowData, Bitmap>?) {
        infoWindow?.let { infoWindow ->
            symbolManager?.delete(infoWindow)
        }
        map?.getStyle { style ->

            style.removeImage(IMAGE_INFO_WINDOW)

            data?.let { (data, bitmap) ->

                style.addImage(IMAGE_INFO_WINDOW, bitmap)

                infoWindow = symbolManager?.create(
                    SymbolOptions()
                        .withLatLng(LatLng(data.latitude, data.longitude))
                        .withIconImage(IMAGE_INFO_WINDOW)
                        .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                        .withIconOffset(arrayOf(0f, -64f))
                )
            }
        }
    }

    private fun removeInfoWindow() {
        infoWindow?.let { infoWindow ->
            symbolManager?.delete(infoWindow)
        }
        infoWindow = null
        map?.getStyle { style ->
            style.removeImage(IMAGE_INFO_WINDOW)
        }
    }

}