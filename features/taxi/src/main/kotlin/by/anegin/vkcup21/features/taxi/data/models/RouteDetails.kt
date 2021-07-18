package by.anegin.vkcup21.features.taxi.data.models

internal data class RouteDetails(
    val route: Route,
    val variants: List<Variant>
) {

    val bestVariant = variants.find { it.category == Category.REGULAR }
        ?: variants.first()

    data class Variant(
        val id: Int,
        val category: Category,
        val cost: Int, // in rubles
        val duration: Int, // in minutes
        val waitingTime: Int // in minutes
    )

    enum class Category {
        FAST, // "Проехать быстро"
        REGULAR, // "Увидеть красоты города"
        UNMANNED // "На беспилотнике"
    }

}