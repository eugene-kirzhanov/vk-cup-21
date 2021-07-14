package by.anegin.vkcup21.internal.util

import android.os.Build
import android.view.View
import android.view.WindowInsetsController

fun View.setLightStatusBar(light: Boolean = true) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            windowInsetsController?.setSystemBarsAppearance(
                if (light) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
            @Suppress("DEPRECATION")
            systemUiVisibility = if (light) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
        }
    }
}

fun View.setLightNavigationBar(light: Boolean = true) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            windowInsetsController?.setSystemBarsAppearance(
                if (light) WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS else 0,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            @Suppress("DEPRECATION")
            systemUiVisibility = if (light) {
                systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
    }
}