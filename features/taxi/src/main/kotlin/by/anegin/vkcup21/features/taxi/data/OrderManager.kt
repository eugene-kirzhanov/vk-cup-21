package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Route
import by.anegin.vkcup21.features.taxi.data.models.RouteDetails

internal interface OrderManager {

    suspend fun calculateRouteDetails(route: Route): RouteDetails

}