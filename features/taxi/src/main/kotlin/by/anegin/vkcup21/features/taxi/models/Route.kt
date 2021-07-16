package by.anegin.vkcup21.features.taxi.models

import com.mapbox.api.directions.v5.models.DirectionsRoute

data class Route(
    val latitude: Double,
    val longitude: Double,
    val direction: DirectionsRoute
)