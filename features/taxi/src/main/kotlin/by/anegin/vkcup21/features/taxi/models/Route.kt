package by.anegin.vkcup21.features.taxi.models

import com.mapbox.api.directions.v5.models.DirectionsRoute

class Route(
    val source: Address,
    val destination: Address,
    val distance: Double,
    val duration: Double,
    val direstions: DirectionsRoute // todo replace with type independent of Mapbox
)