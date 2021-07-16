package by.anegin.vkcup21.features.taxi.tools

interface GeoUtil {

    fun getDistance(srcLatitude: Double, srcLongitude: Double, dstLatitude: Double, dstLongitude: Double): Double

}