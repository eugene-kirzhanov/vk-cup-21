package by.anegin.vkcup21.features.taxi.ui.order

import by.anegin.vkcup21.features.taxi.data.models.Address

internal data class InputQuery(
    val text: String,
    val addressType: Address.Type
)