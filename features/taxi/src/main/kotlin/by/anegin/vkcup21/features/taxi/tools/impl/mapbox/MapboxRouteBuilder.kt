package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.models.Position
import by.anegin.vkcup21.features.taxi.models.Route
import by.anegin.vkcup21.features.taxi.tools.RouteBuilder
import by.anegin.vkcup21.taxi.R
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MapboxRouteBuilder @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RouteBuilder {

    companion object {
        private const val MINIMUM_VALID_ROUTE_DISTANCE = 100 // in meters
    }

    override suspend fun buildRoute(sourceLatLng: Position?, destinationLatLng: Position?): Route? = withContext(ioDispatcher) {
        if (sourceLatLng == null || destinationLatLng == null) return@withContext null
        val sourcePoint = Point.fromLngLat(sourceLatLng.longitude, sourceLatLng.latitude)
        val destPoint = Point.fromLngLat(destinationLatLng.longitude, destinationLatLng.latitude)

        val distance = TurfMeasurement.distance(sourcePoint, destPoint, TurfConstants.UNIT_METERS)
        if (distance < MINIMUM_VALID_ROUTE_DISTANCE) return@withContext null

        try {
            val client = MapboxDirections.builder()
                .origin(sourcePoint)
                .destination(destPoint)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .alternatives(true)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .build()

            val response = makeSuspendCall(client::enqueueCall, client::cancelCall)

            response.routes().firstOrNull()?.let {
                Route(
                    latitude = destinationLatLng.latitude,
                    longitude = destinationLatLng.longitude,
                    direction = it
                )
            }
        } catch (t: Throwable) {
            Timber.w(t)
            null
        }
    }

}