package by.anegin.vkcup21.features.taxi.tools

interface GeoCoder {

    suspend fun reverseGeoCode(latitude: Double, longitude: Double): String?

}