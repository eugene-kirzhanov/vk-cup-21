package by.anegin.vkcup21.features.taxi

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.di.DaggerTaxiComponent
import by.anegin.vkcup21.di.TaxiModuleDependencies
import by.anegin.vkcup21.features.taxi.databinding.FragmentTaxiOrderingBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

class TaxiOrderingFragment : Fragment(R.layout.fragment_taxi_ordering) {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<TaxiOrderingViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentTaxiOrderingBinding::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerTaxiComponent.factory()
            .create(EntryPointAccessors.fromApplication(context, TaxiModuleDependencies::class.java))
            .injectTaxiOrderingFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

}