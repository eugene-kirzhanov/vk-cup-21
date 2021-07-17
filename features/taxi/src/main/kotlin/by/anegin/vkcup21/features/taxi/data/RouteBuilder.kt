package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Position
import by.anegin.vkcup21.features.taxi.data.models.Route

internal interface RouteBuilder {

    suspend fun buildRoute(source: Position, destination: Position): Route?

}