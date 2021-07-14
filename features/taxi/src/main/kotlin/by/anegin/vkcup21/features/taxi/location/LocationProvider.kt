package by.anegin.vkcup21.features.taxi.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    val location: Flow<Location>

}