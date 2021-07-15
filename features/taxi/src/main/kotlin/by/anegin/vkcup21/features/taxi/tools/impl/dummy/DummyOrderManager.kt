package by.anegin.vkcup21.features.taxi.tools.impl.dummy

import by.anegin.vkcup21.features.taxi.models.Route
import by.anegin.vkcup21.features.taxi.models.RouteDetails
import by.anegin.vkcup21.features.taxi.tools.OrderManager
import javax.inject.Inject

class DummyOrderManager @Inject constructor() : OrderManager {

    override suspend fun calculateRouteDetails(route: Route): RouteDetails? {
        return null
    }

}