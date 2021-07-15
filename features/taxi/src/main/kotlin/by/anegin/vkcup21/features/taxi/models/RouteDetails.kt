package by.anegin.vkcup21.features.taxi.models

import com.mapbox.api.directions.v5.models.DirectionsRoute

class RouteDetails(
    val destination: Address,
    val direction: DirectionsRoute,
    val variants: List<Variant>
) {

    val bestVariant: Variant

    init {
        bestVariant = variants.find { it.category == Category.REGULAR }
            ?: variants.first()
    }

    class Variant(
        val category: Category,
        val cost: Int, // in rubles
        val duration: Int // in minutes
    )

    enum class Category {
        FAST, // "Проехать быстро"
        REGULAR, // "Увидеть красоты города"
        UNMANNED // "На беспилотнике"
    }

}