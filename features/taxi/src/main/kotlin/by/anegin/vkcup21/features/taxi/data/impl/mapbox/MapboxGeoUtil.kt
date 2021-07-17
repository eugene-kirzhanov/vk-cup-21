package by.anegin.vkcup21.features.taxi.data.impl.mapbox

import by.anegin.vkcup21.features.taxi.data.GeoUtil
import by.anegin.vkcup21.features.taxi.data.models.Position
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import javax.inject.Inject

internal class MapboxGeoUtil @Inject constructor() : GeoUtil {

    override fun getDistance(source: Position, destination: Position): Double {
        return TurfMeasurement.distance(source.toPoint(), destination.toPoint(), TurfConstants.UNIT_METERS)
    }

}