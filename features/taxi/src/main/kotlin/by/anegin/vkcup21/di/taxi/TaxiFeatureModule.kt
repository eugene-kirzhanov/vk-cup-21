package by.anegin.vkcup21.di.taxi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.di.ViewModelFactory
import by.anegin.vkcup21.di.ViewModelKey
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.features.taxi.tools.GeoUtil
import by.anegin.vkcup21.features.taxi.tools.LocationProvider
import by.anegin.vkcup21.features.taxi.tools.NearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.tools.OrderManager
import by.anegin.vkcup21.features.taxi.tools.RouteBuilder
import by.anegin.vkcup21.features.taxi.tools.impl.dummy.DummyOrderManager
import by.anegin.vkcup21.features.taxi.tools.impl.google.GmsLocationProvider
import by.anegin.vkcup21.features.taxi.tools.impl.google.GoogleNearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.tools.impl.mapbox.MapboxGeoCoder
import by.anegin.vkcup21.features.taxi.tools.impl.mapbox.MapboxGeoUtil
import by.anegin.vkcup21.features.taxi.tools.impl.mapbox.MapboxRouteBuilder
import by.anegin.vkcup21.features.taxi.ui.TaxiOrderingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface TaxiFeatureModule {

    @Binds
    fun bindLocationProvider(impl: GmsLocationProvider): LocationProvider

    @Binds
    fun bindGeoUtil(impl: MapboxGeoUtil): GeoUtil

    @Binds
    fun bindGeoCoder(impl: MapboxGeoCoder): GeoCoder

    @Binds
    fun bindNearbyPlacesProvider(impl: GoogleNearbyPlacesProvider): NearbyPlacesProvider

    @Binds
    fun bindRouteBuilder(impl: MapboxRouteBuilder): RouteBuilder

    @Binds
    fun bindOrderManager(impl: DummyOrderManager): OrderManager

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(TaxiOrderingViewModel::class)
    fun taxiOrderingViewModel(viewModel: TaxiOrderingViewModel): ViewModel

}