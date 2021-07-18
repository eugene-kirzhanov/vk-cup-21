package by.anegin.vkcup21.features.news.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.features.news.di.DaggerNewsComponent
import by.anegin.vkcup21.news.R
import by.anegin.vkcup21.news.databinding.FragmentNewsBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

internal class NewsFragment : Fragment(R.layout.fragment_news) {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<NewsViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentNewsBinding::class.java)

    private var systemBarInsets = Insets.NONE

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerNewsComponent.builder()
            .context(context.applicationContext)
            .mainModuleDependencies(EntryPointAccessors.fromApplication(context, MainModuleDependencies::class.java))
            .build()
            .injectNewsFragment(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setupUi(view)
        setupObservers()
    }

    override fun onDestroyView() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onDestroyView()
    }

    private fun setupUi(view: View) {
        appUiController.setupSystemBars(statusBar = SystemBar.Transparent, navigationBar = SystemBar.Default)

        view.setOnApplyWindowInsetsListener { _, insets ->
            systemBarInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())
            WindowInsetsCompat.CONSUMED.toWindowInsets()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    appNavigator.navigateUp()
                }
            }
        )
    }

    private fun setupObservers() {

    }

}