package by.anegin.vkcup21.features.taxi.ui.order.util

import by.anegin.vkcup21.core.resources.ResourceProvider
import by.anegin.vkcup21.taxi.R

internal object DateTimeUtil {

    fun minutesToHumanReadableTime(resources: ResourceProvider, durationInMinutes: Int): String {
        return if (durationInMinutes < 60) {
            resources.getString(R.string.common_time_in_minutes, durationInMinutes)
        } else {
            val hours = durationInMinutes / 60
            val minutes = durationInMinutes % 60
            buildString {
                append(resources.getString(R.string.common_time_in_hours, hours))
                if (minutes > 0) {
                    append(" ")
                    append(resources.getString(R.string.common_time_in_minutes, minutes))
                }
            }
        }
    }

}