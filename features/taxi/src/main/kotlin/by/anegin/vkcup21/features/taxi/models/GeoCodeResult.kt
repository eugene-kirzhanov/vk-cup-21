package by.anegin.vkcup21.features.taxi.models

sealed class GeoCodeResult {

    class AddressByLocation(
        val query: GeoCodeQuery.AddressByLocation,
        val address: String?
    ) : GeoCodeResult()

}
