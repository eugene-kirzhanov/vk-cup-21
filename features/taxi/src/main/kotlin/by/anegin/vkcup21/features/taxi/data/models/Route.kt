package by.anegin.vkcup21.features.taxi.data.models

import com.mapbox.api.directions.v5.models.DirectionsRoute

internal data class Route(
    val destination: Position,
    val direction: DirectionsRoute
)