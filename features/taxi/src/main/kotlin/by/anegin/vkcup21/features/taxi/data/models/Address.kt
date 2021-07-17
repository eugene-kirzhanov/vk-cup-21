package by.anegin.vkcup21.features.taxi.data.models

internal data class Address(
    val position: Position,
    val type: Type,
    val source: Source,
    val title: String
) {

    enum class Source {
        MY_LOCATION,
        USER_SPECIFIED
    }

    enum class Type {
        SOURCE,
        DESTINATION
    }

}