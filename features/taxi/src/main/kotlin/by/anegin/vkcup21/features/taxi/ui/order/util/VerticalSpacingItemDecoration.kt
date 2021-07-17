package by.anegin.vkcup21.features.taxi.ui.order.util

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView

internal class VerticalSpacingItemDecoration(
    topSpacing: Float = 0f,
    itemSpacing: Float = 0f,
    bottomSpacing: Float = 0f,
) : RecyclerView.ItemDecoration() {

    private val topSpacing = topSpacing.toInt()
    private val halfItemSpacing = (itemSpacing / 2f).toInt()
    private val bottomSpacing = bottomSpacing.toInt()

    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        val lastItemIndex = parent.adapter?.itemCount?.minus(1)
        when (itemPosition) {
            0 -> {
                outRect.top = topSpacing
                outRect.bottom = halfItemSpacing
            }
            lastItemIndex -> {
                outRect.top = halfItemSpacing
                outRect.bottom = bottomSpacing
            }
            else -> {
                outRect.top = halfItemSpacing
                outRect.bottom = halfItemSpacing
            }
        }
    }
}