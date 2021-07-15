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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class MapboxRouteBuilder @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RouteBuilder {

    override suspend fun buildRoute(sourceAddress: Address, destinationAddress: Address): Route? = withContext(ioDispatcher) {
        val sourcePoint = Point.fromLngLat(sourceAddress.longitude, sourceAddress.latitude)
        val destPoint = Point.fromLngLat(destinationAddress.longitude, destinationAddress.latitude)

        try {
            val client = MapboxDirections.builder()
                .origin(sourcePoint)
                .destination(destPoint)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .build()
            val response = MapboxUtil.makeCall(client::enqueueCall, client::cancelCall)
            response.routes().firstOrNull()?.let { route ->
                Route(
                    source = sourceAddress,
                    destination = destinationAddress,
                    distance = route.distance(),
                    duration = route.duration(),
                    direstions = route
                )
            }
        } catch (t: Throwable) {
            Timber.w(t)
            null
        }
    }

}