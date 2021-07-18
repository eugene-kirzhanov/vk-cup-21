package by.anegin.vkcup21.core.util

import android.content.Intent

interface StartActivityForResultDispatcher {

    interface Callback {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
    }

    fun addCallback(callback: Callback)

    fun removeCallback(callback: Callback)

}