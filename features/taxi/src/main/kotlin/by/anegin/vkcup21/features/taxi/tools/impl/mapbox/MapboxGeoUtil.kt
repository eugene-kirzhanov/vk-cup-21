package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import by.anegin.vkcup21.features.taxi.tools.GeoUtil
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import javax.inject.Inject

class MapboxGeoUtil @Inject constructor() : GeoUtil {

    override fun getDistance(srcLatitude: Double, srcLongitude: Double, dstLatitude: Double, dstLongitude: Double): Double {
        val sourcePoint = Point.fromLngLat(srcLongitude, srcLatitude)
        val destPoint = Point.fromLngLat(dstLongitude, dstLatitude)
        return TurfMeasurement.distance(sourcePoint, destPoint, TurfConstants.UNIT_METERS)
    }

}