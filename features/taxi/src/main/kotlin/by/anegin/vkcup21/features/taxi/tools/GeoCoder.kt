package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Place

interface GeoCoder {

    suspend fun reverseGeoCode(latitude: Double, longitude: Double): String?

    suspend fun search(query: String): List<Place>

}