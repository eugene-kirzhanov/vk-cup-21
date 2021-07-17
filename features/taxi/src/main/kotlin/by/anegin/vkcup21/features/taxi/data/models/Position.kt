package by.anegin.vkcup21.features.taxi.data.models

internal data class Position(
    val latitude: Double,
    val longitude: Double
) {

    override fun toString(): String = "$latitude, $longitude"

}