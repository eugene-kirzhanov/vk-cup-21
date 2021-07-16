package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Position
import by.anegin.vkcup21.features.taxi.models.Route

interface RouteBuilder {

    suspend fun buildRoute(sourceLatLng: Position?, destinationLatLng: Position?): Route?

}