package by.anegin.vkcup21.internal.util

import androidx.core.graphics.ColorUtils

object ColorUtil {

    fun isColorLight(color: Int): Boolean {
        val lab = DoubleArray(3)
        ColorUtils.colorToLAB(color, lab)
        return lab[0] > 60.0 // lucky guess ;)
    }

}