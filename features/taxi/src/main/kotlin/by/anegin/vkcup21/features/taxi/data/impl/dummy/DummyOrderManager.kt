package by.anegin.vkcup21.features.taxi.data.impl.dummy

import by.anegin.vkcup21.di.DefaultDispatcher
import by.anegin.vkcup21.features.taxi.data.OrderManager
import by.anegin.vkcup21.features.taxi.data.models.Route
import by.anegin.vkcup21.features.taxi.data.models.RouteDetails
import com.mapbox.api.directions.v5.models.DirectionsRoute
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

internal class DummyOrderManager @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : OrderManager {

    override suspend fun calculateRouteDetails(route: Route) = withContext(defaultDispatcher) {
        RouteDetails(
            route = route,
            variants = calculateVariants(route.direction)
        )
    }

    private fun calculateVariants(direction: DirectionsRoute): List<RouteDetails.Variant> {
        return RouteDetails.Category.values().map { category ->
            val costInRubles = calculateCost(direction.distance(), category)
            val durationInMinutes = calculateDuration(direction.duration(), category)
            RouteDetails.Variant(
                category = category,
                cost = costInRubles,
                duration = durationInMinutes
            )
        }
    }

    private fun calculateCost(distanceInMeters: Double, category: RouteDetails.Category): Int {
        val (landingCost, minDistance, pricePerKm) = when (category) {
            RouteDetails.Category.FAST -> Triple(199, 2, 11)
            RouteDetails.Category.REGULAR -> Triple(99, 2, 9)
            RouteDetails.Category.UNMANNED -> Triple(349, 0, 15)
        }
        val distanceInKm = distanceInMeters / 1000
        return if (distanceInKm <= minDistance) {
            landingCost
        } else {
            val remainingDistance = max(0.0, distanceInKm - minDistance)
            (landingCost + remainingDistance * pricePerKm).toInt()
        }
    }

    private fun calculateDuration(durationInSeconds: Double, category: RouteDetails.Category): Int {
        val durationInMinutes = (durationInSeconds / 60).toInt()
        val duration = when (category) {
            RouteDetails.Category.FAST -> durationInMinutes * 0.7
            RouteDetails.Category.REGULAR -> durationInMinutes
            RouteDetails.Category.UNMANNED -> durationInMinutes * 1.2
        }
        return max(3, duration.toInt())
    }

}