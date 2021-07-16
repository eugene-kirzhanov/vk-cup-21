package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Place

interface NearbyPlacesProvider {

    suspend fun findNearbyPlaces(limit: Int): List<Place>

}