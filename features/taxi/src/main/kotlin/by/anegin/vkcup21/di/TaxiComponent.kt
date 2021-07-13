package by.anegin.vkcup21.di

import by.anegin.vkcup21.features.taxi.TaxiOrderingFragment
import dagger.Component

@Component(
    dependencies = [
        TaxiModuleDependencies::class
    ],
    modules = [
        TaxiFeatureModule::class
    ]
)
interface TaxiComponent {

    fun injectTaxiOrderingFragment(fragment: TaxiOrderingFragment)

    @Component.Factory
    interface Builder {
        fun create(dependencies: TaxiModuleDependencies): TaxiComponent
    }

}