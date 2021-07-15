package by.anegin.vkcup21.features.taxi.geo

interface GeoCodingSource {

    suspend fun reverseGeoCode(latitude: Double, longitude: Double): String?

}