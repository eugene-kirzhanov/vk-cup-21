package by.anegin.vkcup21.features.taxi.geo

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.taxi.R
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MapBoxGeoCoder @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : GeoCoder {

    override suspend fun reverseGeoCode(latitude: Double, longitude: Double): String? = withContext(ioDispatcher) {
        val client = MapboxGeocoding.builder()
            .accessToken(context.getString(R.string.mapbox_access_token))
            .query(Point.fromLngLat(longitude, latitude))
            .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
            .mode(GeocodingCriteria.MODE_PLACES)
            .build()
        val response = client.call()
        response.features().firstOrNull()?.let {
            val text = it.text().orEmpty()
            val address = it.address().orEmpty()
            val placeName = it.placeName().orEmpty()
            when {
                text.isNotEmpty() -> buildString {
                    append(text)
                    if (address.isNotEmpty()) {
                        append(", ").append(address)
                    }
                }
                placeName.isNotEmpty() -> placeName
                else -> null
            }
        }
    }

    private suspend fun MapboxGeocoding.call() = suspendCancellableCoroutine<GeocodingResponse> { continuation ->
        continuation.invokeOnCancellation {
            cancelCall()
        }
        enqueueCall(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                response.body()?.let { body ->
                    continuation.resume(body)
                } ?: run {
                    continuation.resumeWithException(IOException("Empty response body"))
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

}