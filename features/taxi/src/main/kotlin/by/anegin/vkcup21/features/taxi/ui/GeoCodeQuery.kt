package by.anegin.vkcup21.features.taxi.ui

sealed class GeoCodeQuery(
    val source: Address.Source
) {

    class AddressByLocation(
        source: Address.Source,
        val latitude: Double,
        val longitude: Double,
        val addressType: Address.Type
    ) : GeoCodeQuery(source)

}