package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import by.anegin.vkcup21.features.taxi.data.models.Position
import com.mapbox.geojson.Point

internal fun Position.toPoint(): Point = Point.fromLngLat(longitude, latitude)
