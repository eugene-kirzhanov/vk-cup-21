package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Route
import by.anegin.vkcup21.features.taxi.models.RouteDetails

interface OrderManager {

    suspend fun calculateRouteDetails(route: Route): RouteDetails?

}