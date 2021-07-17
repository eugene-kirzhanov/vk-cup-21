package by.anegin.vkcup21.features.taxi.data.models

internal sealed class GeoCodeResult {

    abstract fun toAddress(): Address

    data class AddressByLocation(
        val query: GeoCodeQuery.AddressByLocation,
        val addressTitle: String?
    ) : GeoCodeResult() {

        override fun toAddress() = Address(
            position = query.position,
            type = query.addressType,
            source = query.source,
            title = addressTitle ?: query.position.toString()
        )

    }

    data class AddressByPlace(
        val query: GeoCodeQuery.AddressByPlace,
        val addressTitle: String?
    ) : GeoCodeResult() {

        override fun toAddress() = Address(
            position = query.place.position,
            type = query.addressType,
            source = query.source,
            title = addressTitle ?: query.place.toString()
        )

    }

}
