package by.anegin.vkcup21.features.taxi.models

import com.mapbox.api.directions.v5.models.DirectionsRoute

class Route(
    val destination: Address,
    val direction: DirectionsRoute
)