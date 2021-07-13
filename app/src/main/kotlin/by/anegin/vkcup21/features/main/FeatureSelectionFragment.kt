package by.anegin.vkcup21.features.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.databinding.FragmentFeatureSelectionBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FeatureSelectionFragment : Fragment(R.layout.fragment_feature_selection) {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val binding by viewBinding(FragmentFeatureSelectionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonShowTaxiFeature.setOnClickListener {
            appNavigator.navigateTo(FeatureSelectionFragmentDirections.toTaxiFeature())
        }
    }

}