package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Address
import by.anegin.vkcup21.features.taxi.models.Route

interface RouteBuilder {

    suspend fun buildRoute(sourceAddress: Address?, destinationAddress: Address?): Route?

}