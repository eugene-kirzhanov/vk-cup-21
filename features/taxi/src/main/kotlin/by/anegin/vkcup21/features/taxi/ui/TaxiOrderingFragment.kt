package by.anegin.vkcup21.features.taxi.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.core.util.isDestroyed
import by.anegin.vkcup21.core.util.observe
import by.anegin.vkcup21.di.taxi.DaggerTaxiComponent
import by.anegin.vkcup21.di.taxi.TaxiModuleDependencies
import by.anegin.vkcup21.taxi.R
import by.anegin.vkcup21.taxi.databinding.FragmentTaxiOrderingBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.mapbox.mapboxsdk.plugins.localization.LocalizationPlugin
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class TaxiOrderingFragment : Fragment(R.layout.fragment_taxi_ordering) {

    companion object {
        private const val DEFAULT_ZOOM = 15.0
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

    private var systemBarInsets = 0 to 0 // top, bottom

    private val isPermissionGranted = MutableStateFlow(false)
    private val isMyLocationOnScreen = MutableStateFlow(false)
    private val isMyLocationButtonVisible = combine(isPermissionGranted, isMyLocationOnScreen) { isPermissionGranted, isMyLocationOnScreen ->
        isPermissionGranted && !isMyLocationOnScreen
    }

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null

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
        appUiController.setupSystemBars(statusBar = SystemBar.Transparent, navigationBar = SystemBar.Transparent)

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
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        view.postDelayed(1200) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior?.isHideable = false
        }

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

                updateMapLogoAndAttributionPosition()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {}
        })

        binding.bottomSheetAddressess.buttonCloseAddresses.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
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
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
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

}