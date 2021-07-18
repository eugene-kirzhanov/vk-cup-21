package by.anegin.vkcup21.features.taxi.data

import by.anegin.vkcup21.features.taxi.data.models.Position
import kotlinx.coroutines.flow.StateFlow

internal interface LocationProvider {

    val location: StateFlow<Position?>

}