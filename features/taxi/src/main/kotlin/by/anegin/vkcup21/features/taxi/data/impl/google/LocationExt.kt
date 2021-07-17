package by.anegin.vkcup21.features.taxi.data.impl.google

import android.location.Location
import by.anegin.vkcup21.features.taxi.data.models.Position
import com.mapbox.mapboxsdk.geometry.LatLng

internal fun Location.toPosition() = Position(latitude, longitude)

internal fun Location.toLatLng() = LatLng(latitude, longitude)
