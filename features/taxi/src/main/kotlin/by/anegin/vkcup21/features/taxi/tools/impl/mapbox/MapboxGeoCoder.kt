package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.app.Application
import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.taxi.R
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.QueryType
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.location.DefaultLocationProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

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

        val results = makeSuspendSearch { callback ->
            reverseGeocodingSearchEngine.search(options, callback)
        }

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

}