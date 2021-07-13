package by.anegin.vkcup21.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.features.taxi.TaxiOrderingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TaxiFeatureModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(TaxiOrderingViewModel::class)
    internal abstract fun taxiOrderingViewModel(viewModel: TaxiOrderingViewModel): ViewModel

}