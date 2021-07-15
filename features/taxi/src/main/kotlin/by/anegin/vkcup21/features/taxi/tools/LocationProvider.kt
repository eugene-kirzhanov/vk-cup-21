package by.anegin.vkcup21.features.taxi.tools

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    val location: Flow<Location>

}