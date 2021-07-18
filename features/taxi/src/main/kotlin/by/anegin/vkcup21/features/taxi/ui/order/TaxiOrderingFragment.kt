package by.anegin.vkcup21.features.taxi.ui.order

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
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
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.features.taxi.data.impl.google.toLatLng
import by.anegin.vkcup21.features.taxi.data.impl.mapbox.toLatLng
import by.anegin.vkcup21.features.taxi.data.models.Address
import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.features.taxi.data.models.Route
import by.anegin.vkcup21.features.taxi.di.DaggerTaxiComponent
import by.anegin.vkcup21.features.taxi.di.MapboxAccessToken
import by.anegin.vkcup21.features.taxi.ui.order.adapter.PlacesAdapter
import by.anegin.vkcup21.features.taxi.ui.order.adapter.RouteVariantsAdapter
import by.anegin.vkcup21.features.taxi.ui.order.util.LockableBottomSheetBehavior
import by.anegin.vkcup21.features.taxi.ui.order.util.VerticalSpacingItemDecoration
import by.anegin.vkcup21.features.taxi.util.LocationUtil
import by.anegin.vkcup21.taxi.BuildConfig
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
import com.mapbox.mapboxsdk.geometry.LatLngBounds
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
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal class TaxiOrderingFragment : Fragment(R.layout.fragment_taxi_ordering) {

    companion object {
        private const val DEFAULT_ZOOM = 15.0

        private const val IMAGE_SOURCE_ADDRESS_PIN = "image-source-address-pin"
        private const val IMAGE_DESTINATION_ADDRESS_PIN = "image-destination-address-pin"
        private const val IMAGE_ROUTE_INFO_WINDOW = "image-info-window"

        private const val ROUTE_LAYER_ID = "route-layer-id"
        private const val ROUTE_SOURCE_ID = "route-source-id"

        private val isMapboxInitialized = AtomicBoolean(false)

        private fun ensureMapboxInitialized(context: Context, mapboxAccessToken: String) {
            if (!isMapboxInitialized.getAndSet(true)) {
                Mapbox.getInstance(context, mapboxAccessToken)
                Mapbox.getTelemetry()?.setDebugLoggingEnabled(BuildConfig.DEBUG)
            }
        }
    }

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    @MapboxAccessToken
    lateinit var mapboxAccessToken: String

    private val viewModel by viewModels<TaxiOrderingViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentTaxiOrderingBinding::class.java)

    private var systemBarInsets = Insets.NONE

    private var map: MapboxMap? = null
    private var symbolManager: SymbolManager? = null

    private var sourceAddressMarker: Symbol? = null
    private var destinationAddressMarker: Symbol? = null
    private var isSourceAddressMarkerPlaced = false
    private var isDestinationAddressMarkerPlaced = false

    private var infoWindow: Symbol? = null
    private var routeInfoWindowImageAdded = false

    private var bottomSheetBehavior: LockableBottomSheetBehavior<*>? = null
    private var bottomSheetPreviousState = BottomSheetBehavior.STATE_COLLAPSED

    private var pendingAddressTypeForFocus: Address.Type? = null

    private var awaitingRouteAfterSelectingPlace = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerTaxiComponent.builder()
            .context(context.applicationContext)
            .mainModuleDependencies(EntryPointAccessors.fromApplication(context, MainModuleDependencies::class.java))
            .build()
            .injectTaxiOrderingFragment(this)

        ensureMapboxInitialized(context, mapboxAccessToken)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setupUi(view)
        setupObservers()
        binding.mapView.onCreate(savedInstanceState)
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

    // ===

    private inner class AddressTextWatcher(
        private val addressType: Address.Type
    ) : TextWatcher {

        var isEnabled = true

        override fun afterTextChanged(s: Editable?) {
            if (isEnabled) {
                viewModel.findPlacesByText(s?.toString().orEmpty(), addressType)
                updateClearInputButtonsState()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private val sourceAddressChangeListener = AddressTextWatcher(Address.Type.SOURCE)
    private val destinationAddressChangeListener = AddressTextWatcher(Address.Type.DESTINATION)

    private fun setupUi(view: View) {
        appUiController.setupSystemBars(statusBar = SystemBar.Transparent, navigationBar = SystemBar.Default)

        view.setOnApplyWindowInsetsListener { _, insets ->
            systemBarInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())

            binding.bottomSheetAddressess.apply {
                textAddressesTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBarInsets.top
                }
                buttonCloseAddresses.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = systemBarInsets.top
                }
            }

            WindowInsetsCompat.CONSUMED.toWindowInsets()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED -> {
                            setInputAllowed(false)
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                        viewModel.isInRouteDetailsMode.value -> {
                            viewModel.toggleRouteDetailsMode()
                        }
                        else -> appNavigator.navigateUp()
                    }
                }
            }
        )

        val bottomSheetLayoutParams = binding.bottomSheetAddressess.root.layoutParams as? CoordinatorLayout.LayoutParams
        bottomSheetBehavior = bottomSheetLayoutParams?.behavior as? LockableBottomSheetBehavior<*>
        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                updateBottomSheetUi(slideOffset)
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

        binding.bottomSheetAddressess.apply {
            editSourceAddress.addTextChangedListener(sourceAddressChangeListener)
            editDestinationAddress.addTextChangedListener(destinationAddressChangeListener)

            editSourceAddress.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    textAddressesTitle.setText(R.string.address_source_hint)
                }
                updateClearInputButtonsState()
            }
            editDestinationAddress.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    textAddressesTitle.setText(R.string.address_destination_hint)
                }
                updateClearInputButtonsState()
            }

            sourceAddressOverlay.setOnClickListener {
                pendingAddressTypeForFocus = Address.Type.SOURCE
                textAddressesTitle.setText(R.string.address_source_hint)
                if (viewModel.isInRouteDetailsMode.value) {
                    viewModel.toggleRouteDetailsMode()
                }
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            destinationAddressOverlay.setOnClickListener {
                pendingAddressTypeForFocus = Address.Type.DESTINATION
                textAddressesTitle.setText(R.string.address_destination_hint)
                if (viewModel.isInRouteDetailsMode.value) {
                    viewModel.toggleRouteDetailsMode()
                }
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }

            buttonCloseAddresses.setOnClickListener {
                setInputAllowed(false)
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            buttonClearSourceAddress.setOnClickListener {
                editSourceAddress.setText("")
            }
            buttonClearDestinationAddress.setOnClickListener {
                editDestinationAddress.setText("")
            }

            buttonDone.setOnClickListener {
                if (!viewModel.isInRouteDetailsMode.value) {
                    viewModel.toggleRouteDetailsMode()
                }
            }
        }

        binding.mapView.apply {
            addOnCameraWillChangeListener {
                updateMyLocationButtonVisibility()
            }
            addOnCameraIsChangingListener {
                updateMyLocationButtonVisibility()
            }
            getMapAsync { map ->
                if (!this@TaxiOrderingFragment.isDestroyed()) {
                    onMapReady(map)
                }
            }
        }

        binding.buttonMyLocation.setOnClickListener {
            moveCameraToMyLocation()
        }

        binding.bottomSheetAddressess.recyclerViewPlaces.apply {
            layoutManager = LinearLayoutManager(context)

            adapter = PlacesAdapter { place ->
                val currentAddressType = if (binding.bottomSheetAddressess.editSourceAddress.isFocused) {
                    Address.Type.SOURCE
                } else {
                    Address.Type.DESTINATION
                }
                awaitingRouteAfterSelectingPlace = true
                viewModel.onPlaceSelected(place, currentAddressType)
            }

            addItemDecoration(
                VerticalSpacingItemDecoration(
                    topSpacing = 18.dp,
                    bottomSpacing = 18.dp + systemBarInsets.bottom
                )
            )
        }

        binding.bottomSheetAddressess.recyclerViewVariants.apply {
            layoutManager = LinearLayoutManager(context)

            adapter = RouteVariantsAdapter { variant ->
                viewModel.onRouteVariantSelected(variant)
            }

            addItemDecoration(
                VerticalSpacingItemDecoration(
                    bottomSpacing = 18.dp
                )
            )
        }
    }

    private fun setupObservers() {
        viewModel.isLocationPermissionGranted.observe(viewLifecycleOwner) { isGranted ->
            updateMyLocationState(isGranted)

            // todo put markers to the predefined default position
            // if (!isGranted) {
            // }
        }
        viewModel.isMyLocationButtonVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible && !binding.buttonMyLocation.isOrWillBeShown) {
                binding.buttonMyLocation.show()
            } else if (!isVisible && !binding.buttonMyLocation.isOrWillBeHidden) {
                binding.buttonMyLocation.hide()
            }
        }

        viewModel.sourceAddress.observe(viewLifecycleOwner) { address ->
            val title = address?.title.orEmpty()
            binding.bottomSheetAddressess.editSourceAddress.apply {
                sourceAddressChangeListener.isEnabled = false
                setText(title)
                setSelection(title.length)
                sourceAddressChangeListener.isEnabled = true
            }
            updateClearInputButtonsState()
            address?.let {
                maybeUpdateSourceAddressMarkerByMyLocation(it.position)
                if (it.source == Address.Source.USER_SPECIFIED) {
                    isSourceAddressMarkerPlaced = true
                }
            }
        }
        viewModel.destinationAddress.observe(viewLifecycleOwner) { address ->
            val title = address?.title.orEmpty()
            binding.bottomSheetAddressess.editDestinationAddress.apply {
                destinationAddressChangeListener.isEnabled = false
                setText(title)
                setSelection(title.length)
                destinationAddressChangeListener.isEnabled = true
            }
            updateClearInputButtonsState()
            address?.let {
                maybeUpdateDestinationAddressMarkerByMyLocation(it.position)
                if (it.source == Address.Source.USER_SPECIFIED) {
                    isDestinationAddressMarkerPlaced = true
                }
            }
        }

        viewModel.myLocation.observe(viewLifecycleOwner) { myLocation ->
            if (map != null && myLocation != null) {
                if (!isDestinationAddressMarkerPlaced) {
                    maybeUpdateDestinationAddressMarkerByMyLocation(myLocation)
                }
                if (!isSourceAddressMarkerPlaced) {
                    maybeUpdateSourceAddressMarkerByMyLocation(myLocation)
                }
            }
        }

        viewModel.route.observe(viewLifecycleOwner) { route ->
            updateBottomSheetPeekHeight()

            map?.getStyle { style ->
                val routeGeoJson: GeoJsonSource? = style.getSourceAs(ROUTE_SOURCE_ID)
                route?.result?.direction?.geometry()?.let { geometry ->
                    routeGeoJson?.setGeoJson(LineString.fromPolyline(geometry, PRECISION_6))
                } ?: run {
                    routeGeoJson?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
                }
            }

            if (route != null && awaitingRouteAfterSelectingPlace) {
                awaitingRouteAfterSelectingPlace = false
                if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                route.result?.let {
                    zoomCameraToRoute(it)
                }
            }
        }

        viewModel.places.observe(viewLifecycleOwner) { places ->
            (binding.bottomSheetAddressess.recyclerViewPlaces.adapter as? PlacesAdapter)?.submitList(places)
        }

        viewModel.routeInfoWindow.observe(viewLifecycleOwner, observer = ::showRouteInfoWindow)

        viewModel.routeVariants.observe(viewLifecycleOwner) { routeVariants ->
            (binding.bottomSheetAddressess.recyclerViewVariants.adapter as? RouteVariantsAdapter)?.submitList(routeVariants)
        }
        viewModel.selectedRouteVariant.observe(viewLifecycleOwner) { selectedVariant ->
            selectedVariant?.let {
                binding.bottomSheetAddressess.buttonMakeOrder.text = getString(R.string.order_make_order_for_amount, it.cost)
            }
        }

        viewModel.isInRouteDetailsMode.observe(viewLifecycleOwner) { isInRouteDetailsMode ->
            binding.bottomSheetAddressess.apply {
                if (isInRouteDetailsMode) {
                    bottomSheetBehavior?.isLocked = true
                    groupOrderVariants.isVisible = true
                    recyclerViewPlaces.isVisible = false
                } else {
                    bottomSheetBehavior?.isLocked = false
                    groupOrderVariants.isVisible = false
                    recyclerViewPlaces.isVisible = true
                }
            }
            updateBottomSheetPeekHeight()
        }
    }

    private fun updateBottomSheetUi(slideOffset: Float) {
        val treshold = 0.5f
        val offsetWithTreshold = if (slideOffset > treshold) (slideOffset - treshold) / (1f - treshold) else 0f

        binding.bottomSheetAddressess.apply {

            val addressesContainerTopMargin =
                (slideOffset * (resources.getDimension(R.dimen.addresses_container_expanded_top_margin) + systemBarInsets.top)).toInt()
            val addressesContainerHorizontalMargins =
                (slideOffset * resources.getDimension(R.dimen.addresses_container_expanded_horizontal_margins)).toInt()
            val addressesContainerElevation =
                offsetWithTreshold * resources.getDimension(R.dimen.addresses_container_expanded_elevation)

            addressesContainer.apply {
                elevation = addressesContainerElevation
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = addressesContainerTopMargin
                    leftMargin = addressesContainerHorizontalMargins
                    rightMargin = addressesContainerHorizontalMargins
                }
            }

            textAddressesTitle.apply {
                alpha = offsetWithTreshold
                scaleX = offsetWithTreshold
                scaleY = offsetWithTreshold
            }
            buttonCloseAddresses.apply {
                alpha = offsetWithTreshold
                scaleX = offsetWithTreshold
                scaleY = offsetWithTreshold
            }

            buttonDone.isVisible = viewModel.route.value?.result != null
                && slideOffset == 0f
                && !viewModel.isInRouteDetailsMode.value

            recyclerViewPlaces.alpha = offsetWithTreshold
        }
    }

    // === Location permissions ===

    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onLocationPermissionGranted(granted)
    }

    private fun checkForLocationPermission() {
        val isLocationPermissionGranted = LocationUtil.isLocationPermissionGranted(requireContext())
        viewModel.onLocationPermissionGranted(isLocationPermissionGranted)

        if (!isLocationPermissionGranted && viewModel.shouldRequestLocationPermission()) {
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

            updateMyLocationState(viewModel.isLocationPermissionGranted.value)

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
                            isSourceAddressMarkerPlaced = true
                        } else if (annotation.iconImage == IMAGE_DESTINATION_ADDRESS_PIN) {
                            isDestinationAddressMarkerPlaced = true
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
                            val position = Position(annotation.latLng.latitude, annotation.latLng.longitude)
                            viewModel.findAddressByPosition(position, Address.Source.USER_SPECIFIED, it)

                            if (addressType == Address.Type.SOURCE && !isDestinationAddressMarkerPlaced && viewModel.destinationAddress.value == null) {
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
                            .target(location.toLatLng())
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

    private fun zoomCameraToRoute(route: Route) {
        val routeBounds = LatLngBounds.Builder()
            .include(route.source.toLatLng())
            .include(route.destination.toLatLng())
            .build()
        val padding = 64.dp.toInt()
        map?.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                routeBounds,
                padding,
                padding,
                padding,
                (bottomSheetBehavior?.peekHeight ?: 0) + padding
            )
        )
    }

    private fun updateMyLocationButtonVisibility() {
        viewModel.setMyLocationVisibleOnScreen(
            map?.run {
                locationComponent.lastKnownLocation?.let {
                    projection.visibleRegion.latLngBounds.contains(it.toLatLng())
                } ?: true
            } ?: true
        )
    }

    private fun updateMapLogoAndAttributionPosition() {
        map?.uiSettings?.apply {
            val defaultLogoMargin = resources.getDimension(com.mapbox.mapboxsdk.R.dimen.mapbox_four_dp)
            var bottomMargin = (binding.root.height - binding.bottomSheetAddressess.root.y + defaultLogoMargin).toInt()
            if (bottomMargin < systemBarInsets.bottom) bottomMargin = (systemBarInsets.bottom + defaultLogoMargin).toInt()
            setLogoMargins(logoMarginLeft, logoMarginTop, logoMarginRight, bottomMargin)
            setAttributionMargins(attributionMarginLeft, attributionMarginTop, attributionMarginRight, bottomMargin)
        }
    }

    private fun maybeUpdateSourceAddressMarkerByMyLocation(myLocation: Position) {
        var updated = false
        this.sourceAddressMarker
            ?.apply {
                if (this.latLng.latitude != myLocation.latitude || this.latLng.longitude != myLocation.longitude) {
                    this.latLng = myLocation.toLatLng()
                    symbolManager?.update(this)
                    updated = true
                }
            }
            ?: symbolManager?.create(
                SymbolOptions()
                    .withLatLng(myLocation.toLatLng())
                    .withIconImage(IMAGE_SOURCE_ADDRESS_PIN)
                    .withIconAnchor(Property.ICON_ANCHOR_CENTER)
                    .withDraggable(true)
            )?.also {
                this.sourceAddressMarker = it
                updated = true
            }
        if (updated) {
            isSourceAddressMarkerPlaced = true
            viewModel.findAddressByPosition(myLocation, Address.Source.MY_LOCATION, Address.Type.SOURCE)
        }
    }

    private fun maybeUpdateDestinationAddressMarkerByMyLocation(myLocation: Position) {
        var updated = false
        this.destinationAddressMarker
            ?.apply {
                if (this.latLng.latitude != myLocation.latitude || this.latLng.longitude != myLocation.longitude) {
                    this.latLng = myLocation.toLatLng()
                    symbolManager?.update(this)
                    updated = true
                }
            }
            ?: symbolManager?.create(
                SymbolOptions()
                    .withLatLng(myLocation.toLatLng())
                    .withIconImage(IMAGE_DESTINATION_ADDRESS_PIN)
                    .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                    .withDraggable(true)
            )?.also {
                this.destinationAddressMarker = it
                updated = true
            }
        if (updated) {
            isDestinationAddressMarkerPlaced = true
            viewModel.findAddressByPosition(myLocation, Address.Source.MY_LOCATION, Address.Type.DESTINATION)
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
                    sourceAddressChangeListener.isEnabled = false
                    setText(address.title)
                    setSelection(address.title.length)
                    sourceAddressChangeListener.isEnabled = true
                }
            }
            viewModel.destinationAddress.value?.let { address ->
                binding.bottomSheetAddressess.editDestinationAddress.apply {
                    destinationAddressChangeListener.isEnabled = false
                    setText(address.title)
                    setSelection(address.title.length)
                    destinationAddressChangeListener.isEnabled = true
                }
            }
            activity?.currentFocus?.clearFocus()
            hideKeyboard()
        }
        updateClearInputButtonsState()
    }

    private fun updateBottomSheetPeekHeight() {
        val isRouteAvailable = viewModel.route.value?.result != null
        val isInRouteDetailsMode = viewModel.isInRouteDetailsMode.value
        val (peekHeight, isDoneButtonVisible) = if (isRouteAvailable) {
            if (isInRouteDetailsMode) {
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_with_route_variants) to false
            } else {
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height_with_done_button) to true
            }
        } else {
            resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height) to false
        }
        bottomSheetBehavior?.peekHeight = peekHeight
        binding.bottomSheetAddressess.buttonDone.isVisible = isDoneButtonVisible
    }

    private fun updateClearInputButtonsState() {
        binding.bottomSheetAddressess.apply {
            buttonClearSourceAddress.isVisible = editSourceAddress.isEnabled
                && editSourceAddress.isFocused
                && (editSourceAddress.text?.length ?: 0) > 0
            buttonClearDestinationAddress.isVisible = editDestinationAddress.isEnabled
                && editDestinationAddress.isFocused
                && (editDestinationAddress.text?.length ?: 0) > 0
        }
    }

    private fun showRouteInfoWindow(routeInfoWindowData: Pair<Position, Bitmap>?) {
        infoWindow?.let { infoWindow ->
            symbolManager?.delete(infoWindow)
        }
        map?.getStyle { style ->
            if (routeInfoWindowImageAdded) {
                routeInfoWindowImageAdded = false
                style.removeImage(IMAGE_ROUTE_INFO_WINDOW)
            }

            routeInfoWindowData?.let { (destination, infoWindowBitmap) ->
                style.addImage(IMAGE_ROUTE_INFO_WINDOW, infoWindowBitmap)
                routeInfoWindowImageAdded = true

                infoWindow = symbolManager?.create(
                    SymbolOptions()
                        .withLatLng(destination.toLatLng())
                        .withIconImage(IMAGE_ROUTE_INFO_WINDOW)
                        .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                        .withIconOffset(arrayOf(0f, -64f))
                )
            }
        }
    }

    private fun removeInfoWindow() {
        infoWindow?.let {
            symbolManager?.delete(it)
        }
        infoWindow = null
        if (routeInfoWindowImageAdded) {
            routeInfoWindowImageAdded = false
            map?.getStyle {
                it.removeImage(IMAGE_ROUTE_INFO_WINDOW)
            }
        }
    }

}