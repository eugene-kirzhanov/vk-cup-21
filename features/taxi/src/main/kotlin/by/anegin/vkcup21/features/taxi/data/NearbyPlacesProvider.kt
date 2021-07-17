package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Place

internal interface NearbyPlacesProvider {

    suspend fun findNearbyPlaces(limit: Int): List<Place>

}