package by.anegin.vkcup21.features.taxi.data.models

internal sealed class GeoCodeQuery(
    open val source: Address.Source
) {

    data class AddressByLocation(
        override val source: Address.Source,
        val position: Position,
        val addressType: Address.Type
    ) : GeoCodeQuery(source)

    data class AddressByPlace(
        override val source: Address.Source,
        val place: Place,
        val addressType: Address.Type
    ) : GeoCodeQuery(source)

}