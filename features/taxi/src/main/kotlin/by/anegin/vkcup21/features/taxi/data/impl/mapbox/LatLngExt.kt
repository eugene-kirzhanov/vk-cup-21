package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import by.anegin.vkcup21.features.taxi.data.models.Position
import com.mapbox.mapboxsdk.geometry.LatLng

internal fun Position.toLatLng() = LatLng(latitude, longitude)