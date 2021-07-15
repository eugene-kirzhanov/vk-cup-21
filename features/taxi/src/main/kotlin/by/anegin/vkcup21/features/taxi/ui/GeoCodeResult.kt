package by.anegin.vkcup21.features.taxi.ui

sealed class GeoCodeResult {

    class AddressByLocation(
        val query: GeoCodeQuery.AddressByLocation,
        val address: String?
    ) : GeoCodeResult()

}
