package by.anegin.vkcup21.features.taxi.util

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.mapboxsdk.maps.MapView

class MapBottomSheetBehavior(
    context: Context,
    attrs: AttributeSet?
) : CoordinatorLayout.Behavior<MapView>(context, attrs) {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: MapView, dependency: View): Boolean {
        val dependencyLayoutParams = dependency.layoutParams
        return dependencyLayoutParams is CoordinatorLayout.LayoutParams
            && dependencyLayoutParams.behavior is BottomSheetBehavior
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, mapView: MapView, bottomSheet: View): Boolean {
        val bottomSheetLayoutParams = (bottomSheet.layoutParams as? CoordinatorLayout.LayoutParams) ?: return false
        val bottomSheetBehavior = (bottomSheetLayoutParams.behavior as? BottomSheetBehavior) ?: return false
        mapView.translationY = -(parent.height - bottomSheetBehavior.peekHeight - bottomSheet.y + bottomSheetBehavior.peekHeight)

        return true
    }

}