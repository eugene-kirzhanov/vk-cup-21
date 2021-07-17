package by.anegin.vkcup21.features.taxi.ui.order.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import by.anegin.vkcup21.di.DefaultDispatcher
import by.anegin.vkcup21.taxi.R
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class InfoWindowGenerator @Inject constructor(
    context: Context,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    private val layoutInflater = LayoutInflater.from(context)

    suspend fun generate(text: String) = withContext(defaultDispatcher) {
        val view = generateView(text)
        ensureActive()
        generateBitmap(view)
    }

    @SuppressLint("InflateParams")
    private fun generateView(text: String): View {
        val root = layoutInflater.inflate(R.layout.route_info_layout, null)
        val textViewInfo = root.findViewById<TextView>(R.id.text_info)
        textViewInfo.text = text
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        root.measure(measureSpec, measureSpec)
        return root
    }

    private fun generateBitmap(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        return Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.TRANSPARENT)
            Canvas(this).apply {
                view.draw(this)
            }
        }
    }

}