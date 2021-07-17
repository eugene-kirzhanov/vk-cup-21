package by.anegin.vkcup21.features.taxi.tools

import by.anegin.vkcup21.features.taxi.models.Position
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    val location: Flow<Position>

}