package by.anegin.vkcup21.core.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Activity.hideKeyboard(view: View? = null, callback: ((Boolean) -> Unit)? = null) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val inputView = view ?: currentFocus ?: window.decorView
    val changed = inputMethodManager.hideSoftInputFromWindow(
        inputView.windowToken, 0,
        object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == InputMethodManager.RESULT_HIDDEN || resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN) {
                    callback?.invoke(resultCode == InputMethodManager.RESULT_HIDDEN)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
                }
            }
        }
    )
    if (!changed) {
        callback?.invoke(false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }
}

fun Activity.showKeyboard(view: View? = null, callback: ((Boolean) -> Unit)? = null) {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val inputView = view ?: currentFocus ?: window.decorView
    val changed = inputMethodManager.showSoftInput(
        inputView, 0,
        object : ResultReceiver(Handler(Looper.getMainLooper())) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                if (resultCode == InputMethodManager.RESULT_SHOWN || resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN) {
                    callback?.invoke(resultCode == InputMethodManager.RESULT_SHOWN)
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                }
            }
        }
    )
    if (!changed) {
        callback?.invoke(false)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }
}

fun Fragment.hideKeyboard(view: View? = null, callback: ((Boolean) -> Unit)? = null) {
    activity?.hideKeyboard(view, callback)
}

fun Fragment.showKeyboard(view: View? = null, callback: ((Boolean) -> Unit)? = null) {
    activity?.showKeyboard(view, callback)
}
