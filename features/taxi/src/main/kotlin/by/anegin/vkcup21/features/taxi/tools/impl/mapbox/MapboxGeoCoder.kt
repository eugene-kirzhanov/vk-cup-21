package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.app.Application
import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.taxi.R
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.QueryType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.location.DefaultLocationProvider
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MapboxGeoCoder @Inject constructor(
    context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GeoCoder {

    companion object {
        private val isSearchEngineInitialized = AtomicBoolean(false)

        private fun initSearchSdk(context: Context) {
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

    init {
        initSearchSdk(context)
    }

    override suspend fun reverseGeoCode(latitude: Double, longitude: Double): String? = withContext(ioDispatcher) {
        val reverseGeocodingSearchEngine = MapboxSearchSdk.createReverseGeocodingSearchEngine()
        val options = ReverseGeoOptions.Builder(Point.fromLngLat(longitude, latitude))
            .types(QueryType.ADDRESS)
            .limit(1)
            .build()
        val results = makeSearch(options, reverseGeocodingSearchEngine::search)
        results.firstOrNull()?.let { result ->
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
    }

    private suspend fun <O> makeSearch(
        options: O,
        search: (options: O, callback: SearchCallback) -> SearchRequestTask
    ) = suspendCancellableCoroutine<List<SearchResult>> { continuation ->
        val task = search(
            options,
            object : SearchCallback {
                override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                    continuation.resume(results)
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