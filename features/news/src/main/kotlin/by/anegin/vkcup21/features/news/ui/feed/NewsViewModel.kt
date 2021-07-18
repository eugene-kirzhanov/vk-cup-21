package by.anegin.vkcup21.features.news.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.anegin.vkcup21.features.news.data.FeedRepository
import by.anegin.vkcup21.features.news.data.FeedSettings
import by.anegin.vkcup21.features.news.data.models.Post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

internal class NewsViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val feedSettings: FeedSettings
) : ViewModel() {

    companion object {
        private const val POSTS_IN_REQUEST = 15
        private const val POSTS_PRELOAD_TRESHOLD = 10
    }

    private var nextFrom: String? = feedSettings.getNextFrom()

    private val posts = MutableStateFlow<List<Post>?>(null)

    private val _frontPost = MutableStateFlow<Post?>(null)
    val frontPost = _frontPost.asStateFlow()

    private val _backPost = MutableStateFlow<Post?>(null)
    val backPost = _backPost.asStateFlow()

    private val _inProgress = MutableStateFlow(true)
    val inProgress = _inProgress.asStateFlow()

    private val _postActionErrorEvent = MutableSharedFlow<Boolean>()
    val postActionError = _postActionErrorEvent.asSharedFlow()

    private val _feedRequestErrorEvent = MutableSharedFlow<Any>()
    val feedRequestError = _feedRequestErrorEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            posts.collect {
                if (!it.isNullOrEmpty() && _frontPost.value?.id != it[0].id) {
                    _frontPost.value = it[0]
                }
            }
        }
        viewModelScope.launch {
            posts.collect {
                if ((it?.size ?: 0) > 1 && _backPost.value?.id != it?.getOrNull(1)?.id) {
                    _backPost.value = it?.getOrNull(1)
                }
            }
        }
    }

    fun retryLoadFeed() {
        _inProgress.value = true
        loadFeed()
    }

    private fun loadFeed() {
        viewModelScope.launch {
            try {
                val feed = feedRepository.getRecommendedFeed(POSTS_IN_REQUEST, nextFrom)
                nextFrom = feed.nextFrom
                feedSettings.saveNextFrom(feed.nextFrom)

                val postsList = posts.value?.toMutableList() ?: ArrayList()
                postsList.addAll(feed.posts)
                posts.value = postsList

                _inProgress.value = false
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    Timber.w("Error loading feed: ${t.message}")
                }
                val postsCount = posts.value?.size ?: 0
                if (postsCount == 0) {
                    _feedRequestErrorEvent.emit(Any())
                } else {
                    t.printStackTrace()
                }
            }
        }
    }

    fun likeFirstPost() {
        requestFirstPostAction(true)
    }

    fun skipFirstPost() {
        requestFirstPostAction(false)
    }

    private fun requestFirstPostAction(liked: Boolean) {
        // find first post
        val post = posts.value?.firstOrNull() ?: return

        // send request to like/skip post

        viewModelScope.launch {
            try {
                if (liked) {
                    feedRepository.likePost(post)
                } else {
                    feedRepository.skipPost(post)
                }
                // nothing to do
                Timber.v("Post ${post.id} ${if (liked) "liked" else "skipped"}")
            } catch (t: Throwable) {
                if (t !is CancellationException) {
                    Timber.w("Error requesting post action: ${t.message}")
                }
                _postActionErrorEvent.emit(liked)
            }
        }

        // remove post from list and check if we need to preload posts
        val postsList = posts.value?.toMutableList() ?: ArrayList()
        if (postsList.isNotEmpty()) {
            postsList.remove(post)
            posts.value = postsList
        }
        if (postsList.size < POSTS_PRELOAD_TRESHOLD) {
            loadFeed()
        }
    }

}