package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Route
import com.mapbox.mapboxsdk.geometry.LatLng

interface RouteBuilder {

    suspend fun buildRoute(sourceLatLng: LatLng?, destinationLatLng: LatLng?): Route?

}