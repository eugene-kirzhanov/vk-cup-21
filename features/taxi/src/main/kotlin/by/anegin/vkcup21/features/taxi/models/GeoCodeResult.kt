package by.anegin.vkcup21.features.taxi.models

sealed class GeoCodeResult {

    data class AddressByLocation(
        val query: GeoCodeQuery.AddressByLocation,
        val addressTitle: String?
    ) : GeoCodeResult()

    data class AddressByPlace(
        val query: GeoCodeQuery.AddressByPlace,
        val addressTitle: String?
    ) : GeoCodeResult()

}
