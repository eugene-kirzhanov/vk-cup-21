package by.anegin.vkcup21.features.taxi.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.di.ViewModelFactory
import by.anegin.vkcup21.di.ViewModelKey
import by.anegin.vkcup21.features.taxi.data.GeoCoder
import by.anegin.vkcup21.features.taxi.data.GeoUtil
import by.anegin.vkcup21.features.taxi.data.LocationProvider
import by.anegin.vkcup21.features.taxi.data.NearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.data.OrderManager
import by.anegin.vkcup21.features.taxi.data.RouteBuilder
import by.anegin.vkcup21.features.taxi.data.impl.dummy.DummyOrderManager
import by.anegin.vkcup21.features.taxi.data.impl.google.GmsLocationProvider
import by.anegin.vkcup21.features.taxi.data.impl.google.GoogleNearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.data.impl.mapbox.MapboxGeoCoder
import by.anegin.vkcup21.features.taxi.data.impl.mapbox.MapboxGeoUtil
import by.anegin.vkcup21.features.taxi.data.impl.mapbox.MapboxRouteBuilder
import by.anegin.vkcup21.features.taxi.ui.order.TaxiOrderingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("unused")
internal interface TaxiFeatureModule {

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