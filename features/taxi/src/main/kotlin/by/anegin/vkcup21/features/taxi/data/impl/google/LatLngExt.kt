package by.anegin.vkcup21.features.taxi.data.impl.google

import by.anegin.vkcup21.features.taxi.data.models.Position
import com.google.android.gms.maps.model.LatLng

internal fun LatLng.toPosition() = Position(latitude, longitude)