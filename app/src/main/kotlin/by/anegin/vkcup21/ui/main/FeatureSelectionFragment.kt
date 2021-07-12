package by.anegin.vkcup21.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.databinding.FragmentFeatureSelectionBinding
import by.anegin.vkcup21.nav.AppNavigator
import by.kirich1409.viewbindingdelegate.viewBinding
import javax.inject.Inject

class FeatureSelectionFragment : Fragment(R.layout.fragment_feature_selection) {

    @Inject
    lateinit var appNavigator: AppNavigator

    private val binding by viewBinding(FragmentFeatureSelectionBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonShowTaxiFeature.setOnClickListener {
            // todo navigate to Taxi feature
        }
    }

}