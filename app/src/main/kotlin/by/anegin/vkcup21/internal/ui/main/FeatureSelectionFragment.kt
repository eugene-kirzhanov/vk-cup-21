package by.anegin.vkcup21.internal.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.databinding.FragmentFeatureSelectionBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeatureSelectionFragment : Fragment(R.layout.fragment_feature_selection) {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    private val binding by viewBinding(FragmentFeatureSelectionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appUiController.setupSystemBars(statusBar = SystemBar.Default, navigationBar = SystemBar.Default)

        binding.buttonShowTaxiFeature.setOnClickListener {
            appNavigator.navigateTo(FeatureSelectionFragmentDirections.toTaxiFeature())
        }
    }

}