package by.anegin.vkcup21.features.taxi.ui.order.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import by.anegin.vkcup21.core.resources.ResourceProvider
import by.anegin.vkcup21.features.taxi.data.models.RouteDetails
import by.anegin.vkcup21.features.taxi.ui.order.util.DateTimeUtil
import by.anegin.vkcup21.taxi.R

internal data class RouteVariant(
    val id: Int,
    @DrawableRes val categoryIcon: Int,
    @StringRes val categoryTitle: Int,
    val info: String,
    val cost: String,
    val isSelected: Boolean
)

internal fun RouteDetails.Variant.toRouteVariant(resources: ResourceProvider, isSelected: Boolean): RouteVariant {
    val categoryIcon = when (category) {
        RouteDetails.Category.FAST -> R.drawable.car_fast
        RouteDetails.Category.REGULAR -> R.drawable.car_regular
        RouteDetails.Category.UNMANNED -> R.drawable.car_unmanned
    }
    val categoryTitle = when (category) {
        RouteDetails.Category.FAST -> R.string.route_category_fast_title
        RouteDetails.Category.REGULAR -> R.string.route_category_regular_title
        RouteDetails.Category.UNMANNED -> R.string.route_category_unmanned_title
    }

    val durationInMinutes = DateTimeUtil.minutesToHumanReadableTime(resources, duration)
    val waitingInMinutes = DateTimeUtil.minutesToHumanReadableTime(resources, waitingTime)
    val info = resources.getString(R.string.route_info, durationInMinutes, waitingInMinutes)

    val cost = resources.getString(R.string.common_cost_in_rubles, cost)

    return RouteVariant(
        id = id,
        categoryIcon = categoryIcon,
        categoryTitle = categoryTitle,
        info = info,
        cost = cost,
        isSelected = isSelected
    )

}