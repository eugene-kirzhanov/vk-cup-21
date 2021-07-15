package by.anegin.vkcup21.di.taxi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.di.ViewModelFactory
import by.anegin.vkcup21.di.ViewModelKey
import by.anegin.vkcup21.features.taxi.geo.GeoCoder
import by.anegin.vkcup21.features.taxi.geo.MapBoxGeoCoder
import by.anegin.vkcup21.features.taxi.location.GmsLocationProvider
import by.anegin.vkcup21.features.taxi.location.LocationProvider
import by.anegin.vkcup21.features.taxi.ui.TaxiOrderingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface TaxiFeatureModule {

    @Binds
    fun bindLocationProvider(impl: GmsLocationProvider): LocationProvider

    @Binds
    fun bindGeoCodingSource(impl: MapBoxGeoCoder): GeoCoder

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(TaxiOrderingViewModel::class)
    fun taxiOrderingViewModel(viewModel: TaxiOrderingViewModel): ViewModel

}