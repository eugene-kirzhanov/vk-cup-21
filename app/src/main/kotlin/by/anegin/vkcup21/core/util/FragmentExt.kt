package by.anegin.vkcup21.core.util

import androidx.fragment.app.Fragment

fun Fragment.isDestroyed(): Boolean {
    return !isAdded || isDetached || isRemoving || view?.windowToken == null
}