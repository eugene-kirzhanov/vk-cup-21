package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.models.Address
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

    override suspend fun buildRoute(sourceAddress: Address?, destinationAddress: Address?): Route? = withContext(ioDispatcher) {
        if (sourceAddress == null || destinationAddress == null) return@withContext null
        val sourcePoint = Point.fromLngLat(sourceAddress.longitude, sourceAddress.latitude)
        val destPoint = Point.fromLngLat(destinationAddress.longitude, destinationAddress.latitude)

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
            val response = MapboxUtil.makeCall(client::enqueueCall, client::cancelCall)

            response.routes().firstOrNull()?.let {
                Route(
                    destination = destinationAddress,
                    direction = it
                )
            }
        } catch (t: Throwable) {
            Timber.w(t)
            null
        }
    }

}