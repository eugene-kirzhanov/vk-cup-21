package by.anegin.vkcup21.features.taxi.models

class Address(
    val latitude: Double,
    val longitude: Double,
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