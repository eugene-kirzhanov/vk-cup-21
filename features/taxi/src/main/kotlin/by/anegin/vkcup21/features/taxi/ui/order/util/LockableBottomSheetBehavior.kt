package by.anegin.vkcup21.features.taxi.ui.order.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

@Suppress("unused")
internal class LockableBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    var isLocked = false

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return if (isLocked) {
            false
        } else {
            super.onInterceptTouchEvent(parent, child, event)
        }
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return if (isLocked) {
            false
        } else {
            super.onTouchEvent(parent, child, event)
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return if (isLocked) {
            false
        } else {
            super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (!isLocked) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, type: Int) {
        if (!isLocked) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View, velocityX: Float, velocityY: Float): Boolean {
        return if (isLocked) {
            false
        } else {
            super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        }
    }

}