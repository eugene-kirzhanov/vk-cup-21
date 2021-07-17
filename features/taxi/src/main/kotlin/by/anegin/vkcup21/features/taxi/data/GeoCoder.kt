package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.features.taxi.data.models.Position

internal interface GeoCoder {

    suspend fun reverseGeoCode(position: Position): String?

    suspend fun search(query: String): List<Place>

}