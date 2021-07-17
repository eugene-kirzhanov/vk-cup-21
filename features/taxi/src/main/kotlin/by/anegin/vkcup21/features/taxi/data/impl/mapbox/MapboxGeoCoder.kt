package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import android.app.Application
import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.data.GeoCoder
import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.taxi.R
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.QueryType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.location.DefaultLocationProvider
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class MapboxGeoCoder @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GeoCoder {

    companion object {
        private val isSearchEngineInitialized = AtomicBoolean(false)

        private fun ensureSearchSdkInitialized(context: Context) {
            if (!isSearchEngineInitialized.getAndSet(true)) {
                (context.applicationContext as Application).apply {
                    MapboxSearchSdk.initialize(
                        this,
                        context.getString(R.string.mapbox_access_token),
                        DefaultLocationProvider(this)
                    )
                }
            }
        }
    }

    override suspend fun reverseGeoCode(position: Position): String? = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            ensureSearchSdkInitialized(context)

            val reverseGeocodingSearchEngine = MapboxSearchSdk.createReverseGeocodingSearchEngine()

            val options = ReverseGeoOptions.Builder(position.toPoint())
                .types(QueryType.ADDRESS)
                .limit(1)
                .build()

            val task = reverseGeocodingSearchEngine.search(
                options,
                object : SearchCallback {
                    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                        val result = results.firstOrNull()?.let { result ->
                            result.address?.street?.let { street ->
                                val houseNumber = result.address?.houseNumber.orEmpty()
                                buildString {
                                    append(street)
                                    if (houseNumber.isNotEmpty()) {
                                        append(", ").append(houseNumber)
                                    }
                                }
                            } ?: result.name
                        }
                        continuation.resume(result)
                    }

                    override fun onError(e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            )

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

    override suspend fun search(query: String): List<Place> = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            ensureSearchSdkInitialized(context)

            val searchEngine = MapboxSearchSdk.createSearchEngine()

            val options = SearchOptions.Builder()
                .limit(5)
                .build()

            val task = searchEngine.search(
                query, options,
                object : SearchSelectionCallback {
                    override fun onCategoryResult(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {
                        Timber.w("onCategoryResult()")
                    }

                    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                        Timber.w("onSuggestions()")
                    }

                    override fun onResult(suggestion: SearchSuggestion, result: SearchResult, responseInfo: ResponseInfo) {
                        Timber.w("onResult()")
                        continuation.resume(emptyList())
                    }

                    override fun onError(e: Exception) {
                        Timber.w("onError()")
                        continuation.resumeWithException(e)
                    }
                }
            )

            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }

}