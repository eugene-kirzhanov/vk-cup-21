package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.data.RouteBuilder
import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.features.taxi.data.models.Route
import by.anegin.vkcup21.taxi.R
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
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

internal class MapboxRouteBuilder @Inject constructor(
    private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RouteBuilder {

    companion object {
        private const val MINIMUM_VALID_ROUTE_DISTANCE = 100 // in meters
    }

    override suspend fun buildRoute(source: Position, destination: Position): Route? = withContext(ioDispatcher) {
        suspendCancellableCoroutine { continuation ->

            val sourcePoint = source.toPoint()
            val destPoint = destination.toPoint()

            val distance = TurfMeasurement.distance(sourcePoint, destPoint, TurfConstants.UNIT_METERS)
            if (distance < MINIMUM_VALID_ROUTE_DISTANCE) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val client = MapboxDirections.builder()
                .origin(sourcePoint)
                .destination(destPoint)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .alternatives(true)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .build()

            continuation.invokeOnCancellation {
                client.cancelCall()
            }

            try {
                client.enqueueCall(object : Callback<DirectionsResponse> {
                    override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                        response.body()?.let { body ->
                            val route = body.routes().firstOrNull()?.let {
                                Route(
                                    destination = destination,
                                    direction = it
                                )
                            }
                            continuation.resume(route)
                        } ?: run {
                            continuation.resumeWithException(IOException("Empty response body"))
                        }
                    }

                    override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
            } catch (t: Throwable) {
                continuation.resumeWithException(t)
            }
        }
    }

}