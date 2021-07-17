package by.anegin.vkcup21.features.taxi.di

import android.content.Context
import by.anegin.vkcup21.di.RootModule
import by.anegin.vkcup21.di.taxi.TaxiModuleDependencies
import by.anegin.vkcup21.features.taxi.ui.order.TaxiOrderingFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        TaxiModuleDependencies::class
    ],
    modules = [
        RootModule::class,
        TaxiFeatureModule::class
    ]
)
internal interface TaxiComponent {

    fun injectTaxiOrderingFragment(fragment: TaxiOrderingFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun taxiModuleDependencies(dependencies: TaxiModuleDependencies): Builder
        fun build(): TaxiComponent
    }

}