package by.anegin.vkcup21.features.news.ui.feed

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.core.util.observe
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.features.news.data.models.Attachment
import by.anegin.vkcup21.features.news.data.models.Audio
import by.anegin.vkcup21.features.news.data.models.Link
import by.anegin.vkcup21.features.news.data.models.PostSource
import by.anegin.vkcup21.features.news.data.models.Video
import by.anegin.vkcup21.features.news.di.DaggerNewsComponent
import by.anegin.vkcup21.features.news.di.VkAppId
import by.anegin.vkcup21.features.news.ui.feed.view.SwipeableFeedLayout
import by.anegin.vkcup21.news.R
import by.anegin.vkcup21.news.databinding.FragmentNewsBinding
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiConfig
import com.vk.api.sdk.VKDefaultValidationHandler
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal class NewsFragment : Fragment(R.layout.fragment_news), SwipeableFeedLayout.Listener {

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

    private lateinit var glide: RequestManager

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

        glide = Glide.with(this)
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
        } else {
            if (viewModel.frontPost.value == null) {
                viewModel.retryLoadFeed()
            }
        }
    }

    override fun onDestroyView() {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onDestroyView()
    }

    // ======== SwipeableFeedLayout.Listener =========

    override fun onSwipe(value: Float) {
        getFrontViewHolder()?.onSwipe(value)
        getBackViewHolder()?.reset()
    }

    override fun onSwipeCanceled() {
        getFrontViewHolder()?.onSwipe(0f)
        getBackViewHolder()?.reset()
    }

    override fun onSwipedToRight() {
        getBackViewHolder()?.reset()

        binding.swipeableFeedLayout.setSwipeDisabled(false)
        binding.lockableScrollView.setScrollEnabled(false)

        viewModel.likeFirstPost()
    }

    override fun onSwipedToLeft() {
        getBackViewHolder()?.reset()

        binding.swipeableFeedLayout.setSwipeDisabled(false)
        binding.lockableScrollView.setScrollEnabled(false)

        viewModel.skipFirstPost()
    }

    // =================

    private fun getBackViewHolder() = getPostViewHolder(binding.swipeableFeedLayout.getBackLayout())

    private fun getFrontViewHolder() = getPostViewHolder(binding.swipeableFeedLayout.getFrontLayout())

    private fun getPostViewHolder(itemView: View?): PostViewHolder? {
        var holder = itemView?.tag as? PostViewHolder
        if (holder == null && itemView != null) {
            holder = PostViewHolder(
                itemView, glide,
                { isExpanded, isAnimating ->
                    val verticalScrollEnabled = isAnimating || isExpanded
                    binding.swipeableFeedLayout.setSwipeDisabled(verticalScrollEnabled)
                    binding.lockableScrollView.setScrollEnabled(verticalScrollEnabled)
                },
                { openPostSource(it) },
                { openAttachment(it) }
            )
            itemView.tag = holder
        }
        return holder
    }

    // =================

    private fun setupUi(view: View) {
        appUiController.setupSystemBars(statusBar = SystemBar.Colored(Color.WHITE), navigationBar = SystemBar.Colored(Color.WHITE))

        view.setOnApplyWindowInsetsListener { _, insets ->
            systemBarInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                .getInsets(WindowInsetsCompat.Type.systemBars())

            binding.appbar.updatePadding(top = systemBarInsets.top)

            binding.buttonSkip.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBarInsets.bottom
            }
            binding.buttonLike.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBarInsets.bottom
            }

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

        binding.swipeableFeedLayout.setListener(this)

        binding.buttonSkip.setOnClickListener {
            binding.swipeableFeedLayout.swipeToLeft()
        }
        binding.buttonLike.setOnClickListener {
            binding.swipeableFeedLayout.swipeToRight()
        }

        binding.buttonRetry.setOnClickListener {
            showProgress()
            viewModel.retryLoadFeed()
        }
    }

    private fun setupObservers() {
        viewModel.frontPost.observe(viewLifecycleOwner) {
            getFrontViewHolder()?.bind(it)
        }
        viewModel.backPost.observe(viewLifecycleOwner) {
            getBackViewHolder()?.bind(it)
        }
        viewModel.inProgress.observe(viewLifecycleOwner) {
            if (it) {
                showProgress()
            } else {
                hideProgress()
            }
        }
        viewModel.postActionError.observe(viewLifecycleOwner) {
            Toast.makeText(context, R.string.request_failed, Toast.LENGTH_SHORT).show()
        }
        viewModel.feedRequestError.observe(viewLifecycleOwner) {
            showRequestFeedError()
        }
    }

    private fun logout() {
        VK.logout()
        appNavigator.navigateUp()
    }

    private fun showProgress() {
        binding.groupContent.visibility = View.INVISIBLE
        binding.groupError.visibility = View.GONE
        binding.groupProgress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.groupContent.visibility = View.VISIBLE
        binding.groupError.visibility = View.GONE
        binding.groupProgress.visibility = View.GONE
    }

    private fun showRequestFeedError() {
        binding.groupContent.visibility = View.INVISIBLE
        binding.groupError.visibility = View.VISIBLE
        binding.groupProgress.visibility = View.GONE
    }

    private fun openPostSource(postSource: PostSource?) {
        if (postSource == null) return
        openUrl(postSource.getSourceUrl())
    }

    private fun openAttachment(attachment: Attachment?) {
        when (attachment) {
            is Video -> openUrl(attachment.getVideoUrl())
            is Audio -> openUrl(attachment.getAudioUrl())
            is Link -> openUrl(attachment.getUrl())
        }
    }

    private fun openUrl(url: String): Boolean {
        val customTabsIntent = CustomTabsIntent.Builder()
            .build()
        customTabsIntent.intent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://" + requireContext().packageName)
        )
        return try {
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
            true
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            false
        }
    }

}