package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.tools.GeoCoder
import by.anegin.vkcup21.taxi.R
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MapboxGeoCoder @Inject constructor(
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
        val response = MapboxUtil.makeCall(client::enqueueCall, client::cancelCall)
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

}