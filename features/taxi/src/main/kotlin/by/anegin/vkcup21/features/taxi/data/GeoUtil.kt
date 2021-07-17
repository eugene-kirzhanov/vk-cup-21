package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Position

internal interface GeoUtil {

    fun getDistance(source: Position, destination: Position): Double

}