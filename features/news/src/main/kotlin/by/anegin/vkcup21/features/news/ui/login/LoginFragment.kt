package by.anegin.vkcup21.features.news.ui.login

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.core.util.StartActivityForResultDispatcher
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.features.news.di.DaggerNewsComponent
import by.anegin.vkcup21.news.R
import by.anegin.vkcup21.news.databinding.FragmentLoginBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.snackbar.Snackbar
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import dagger.hilt.android.EntryPointAccessors
import javax.inject.Inject

internal class LoginFragment : Fragment(R.layout.fragment_login) {

    @Inject
    lateinit var appNavigator: AppNavigator

    @Inject
    lateinit var appUiController: AppUiController

    private val binding by viewBinding(FragmentLoginBinding::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        DaggerNewsComponent.builder()
            .context(context.applicationContext)
            .mainModuleDependencies(EntryPointAccessors.fromApplication(context, MainModuleDependencies::class.java))
            .build()
            .injectLoginFragment(this)

        (requireActivity() as? StartActivityForResultDispatcher)?.addCallback(startActivityForResultCallback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        appUiController.setupSystemBars(statusBar = SystemBar.Transparent, navigationBar = SystemBar.Colored(Color.WHITE))

        view.setOnApplyWindowInsetsListener { _, insets ->
            val systemBarInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appbar.updatePadding(top = systemBarInsets.top)

            WindowInsetsCompat.CONSUMED.toWindowInsets()
        }

        binding.toolbar.setNavigationOnClickListener {
            appNavigator.navigateUp()
        }

        binding.buttonLogin.setOnClickListener {
            login()
        }
    }

    override fun onDestroyView() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onDestroyView()
    }

    override fun onDetach() {
        (requireActivity() as? StartActivityForResultDispatcher)?.removeCallback(startActivityForResultCallback)
        super.onDetach()
    }

    private fun login() {
        VK.logout()
        VK.login(requireActivity(), scopes = listOf(VKScope.WALL, VKScope.PHOTOS, VKScope.FRIENDS))
    }

    private val startActivityForResultCallback = object : StartActivityForResultDispatcher.Callback {
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            if (data == null) return false
            return VK.onActivityResult(
                requestCode, resultCode, data,
                object : VKAuthCallback {
                    override fun onLogin(token: VKAccessToken) {
                        appNavigator.navigateUp()
                    }

                    override fun onLoginFailed(errorCode: Int) {
                        view?.let {
                            Snackbar.make(it, R.string.login_failed, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

}