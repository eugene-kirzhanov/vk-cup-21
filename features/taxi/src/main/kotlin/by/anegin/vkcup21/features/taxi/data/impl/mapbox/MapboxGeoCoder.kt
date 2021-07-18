package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import android.app.Application
import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.data.GeoCoder
import by.anegin.vkcup21.features.taxi.data.LocationProvider
import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.features.taxi.di.MapboxAccessToken
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.QueryType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchMultipleSelectionCallback
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSuggestionsCallback
import com.mapbox.search.result.SearchAddress
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchResultType
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class MapboxGeoCoder @Inject constructor(
    private val context: Context,
    private val locationProvider: LocationProvider,
    @MapboxAccessToken private val mapboxAccessToken: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GeoCoder {

    companion object {
        private val isSearchEngineInitialized = AtomicBoolean(false)

        private fun ensureSearchSdkInitialized(context: Context, mapboxAccessToken: String, locationProvider: LocationProvider) {
            if (!isSearchEngineInitialized.getAndSet(true)) {
                (context.applicationContext as Application).apply {
                    MapboxSearchSdk.initialize(
                        this,
                        mapboxAccessToken,
                        {
                            // proxy app LocationProvider data to Mapbox LocationProvider
                            locationProvider.location.value?.toPoint()
                        }
                    )
                }
            }
        }
    }

    override suspend fun reverseGeoCode(position: Position): String? = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->
            ensureSearchSdkInitialized(context, mapboxAccessToken, locationProvider)

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
                            result.address?.asString() ?: result.name
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

    override suspend fun geocode(query: String): List<Place> = withContext(ioDispatcher) {

        ensureSearchSdkInitialized(context, mapboxAccessToken, locationProvider)

        val searchEngine = MapboxSearchSdk.createSearchEngine()

        val options = SearchOptions.Builder()
            .limit(30)
            .build()

        val suggestions = searchEngine.suspendSearch(query, options)
        val results = searchEngine.suspendSelect(suggestions)

        results
            .filter { item ->
                // filter suggestions by certain types
                item.types.contains(SearchResultType.PLACE)
                    || item.types.contains(SearchResultType.ADDRESS)
                    || item.types.contains(SearchResultType.POI)
                    || item.types.contains(SearchResultType.STREET)
            }
            .mapNotNull { item ->
                item.coordinate?.let { point ->
                    Place(
                        position = Position(point.latitude(), point.longitude()),
                        title = item.name,
                        address = item.address?.asString().orEmpty()
                    )
                }
            }
            .distinctBy { place ->
                // remove places with equal address
                place.address
            }
    }

    private fun SearchAddress.asString(): String? {
        return street?.let { street ->
            val houseNumber = houseNumber.orEmpty()
            buildString {
                append(street)
                if (houseNumber.isNotEmpty()) {
                    append(", ").append(houseNumber)
                }
            }
        }
    }

    private suspend fun SearchEngine.suspendSearch(query: String, options: SearchOptions): List<SearchSuggestion> =
        suspendCancellableCoroutine { continuation ->
            val task = search(
                query, options,
                object : SearchSuggestionsCallback {
                    override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                        continuation.resume(suggestions)
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

    private suspend fun SearchEngine.suspendSelect(suggestions: List<SearchSuggestion>): List<SearchResult> =
        suspendCancellableCoroutine { continuation ->
            val task = select(
                suggestions,
                object : SearchMultipleSelectionCallback {
                    override fun onResult(suggestions: List<SearchSuggestion>, results: List<SearchResult>, responseInfo: ResponseInfo) {
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
