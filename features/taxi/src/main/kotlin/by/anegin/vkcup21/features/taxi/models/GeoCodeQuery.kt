package by.anegin.vkcup21.features.taxi.models

sealed class GeoCodeQuery(
    open val source: Address.Source
) {

    data class AddressByLocation(
        override val source: Address.Source,
        val latitude: Double,
        val longitude: Double,
        val addressType: Address.Type
    ) : GeoCodeQuery(source)

    data class AddressByPlace(
        override val source: Address.Source,
        val place: Place,
        val addressType: Address.Type
    ) : GeoCodeQuery(source)

}