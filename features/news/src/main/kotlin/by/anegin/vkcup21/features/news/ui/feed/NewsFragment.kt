package by.anegin.vkcup21.features.news.ui.feed

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.features.news.di.DaggerNewsComponent
import by.anegin.vkcup21.features.news.di.VkAppId
import by.anegin.vkcup21.news.R
import by.anegin.vkcup21.news.databinding.FragmentNewsBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal class NewsFragment : Fragment(R.layout.fragment_news) {

    companion object {
        private val isVkSdkInitialized = AtomicBoolean(false)

        private fun ensureVkSdkInitialized(context: Context, vkAppId: String) {
            if (!isVkSdkInitialized.getAndSet(true)) {
                VK.setConfig(
                    VKApiConfig(
                        context = context,
                        appId = vkAppId.toInt(),
                        validationHandler = VKDefaultValidationHandler(context)
                    )
                )
            }
        }
    }

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    @VkAppId
    lateinit var vkAppId: String

    private val viewModel by viewModels<NewsViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentNewsBinding::class.java)

    private var systemBarInsets = Insets.NONE

    private var loginRequested = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerNewsComponent.builder()
            .context(context.applicationContext)
            .mainModuleDependencies(EntryPointAccessors.fromApplication(context, MainModuleDependencies::class.java))
            .build()
            .injectNewsFragment(this)

        ensureVkSdkInitialized(context, vkAppId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        setupUi(view)
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        if (!VK.isLoggedIn()) {
            if (!loginRequested) {
                loginRequested = true
                appNavigator.navigateTo(NewsFragmentDirections.toLogin())
            } else {
                appNavigator.navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onDestroyView()
    }

    private fun setupUi(view: View) {
        appUiController.setupSystemBars(statusBar = SystemBar.Colored(Color.WHITE), navigationBar = SystemBar.Colored(Color.WHITE))

        view.setOnApplyWindowInsetsListener { _, insets ->
            systemBarInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appbar.updatePadding(top = systemBarInsets.top)

            WindowInsetsCompat.CONSUMED.toWindowInsets()
        }

        binding.toolbar.apply {
            setNavigationOnClickListener {
                appNavigator.navigateUp()
            }
            binding.toolbar.menu?.clear()
            inflateMenu(R.menu.news_feed)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_logout -> logout()
                }
                true
            }
        }
    }

    private fun setupObservers() {

    }

    private fun logout() {
        VK.logout()
        appNavigator.navigateUp()
    }

}