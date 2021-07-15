package by.anegin.vkcup21.features.taxi.geo

interface GeoCoder {

    suspend fun reverseGeoCode(latitude: Double, longitude: Double): String?

}