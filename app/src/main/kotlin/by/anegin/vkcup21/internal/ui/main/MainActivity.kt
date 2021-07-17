package by.anegin.vkcup21.internal.ui.main

import android.graphics.Color
import android.os.Bundle
import androidx.annotation.AttrRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.core.nav.AppNavigatorSource
import by.anegin.vkcup21.core.nav.AppUiControllerSource
import by.anegin.vkcup21.core.nav.NavEvent
import by.anegin.vkcup21.core.nav.SystemBar
import by.anegin.vkcup21.core.util.observe
import by.anegin.vkcup21.internal.util.ColorUtil
import by.anegin.vkcup21.internal.util.setLightNavigationBar
import by.anegin.vkcup21.internal.util.setLightStatusBar
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
internal class MainActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    internal lateinit var navigatorSource: AppNavigatorSource

    @Inject
    internal lateinit var uiControllerSource: AppUiControllerSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        navigatorSource.navigationEvents.observe(this) { event ->
            when (event) {
                is NavEvent.Destination -> navController.navigate(event.direction)
                NavEvent.Up -> navController.navigateUp()
            }
        }

        uiControllerSource.statusBar.observe(this) { statusBar ->
            val (color, isColorLight) = pickSystemBarColor(statusBar, R.attr.colorPrimaryDark)
            window.statusBarColor = color
            window.decorView.setLightStatusBar(isColorLight)
        }

        uiControllerSource.navigationBar.observe(this) { navigationBar ->
            val (color, isColorLight) = pickSystemBarColor(navigationBar, R.attr.backgroundColor)
            window.navigationBarColor = color
            window.decorView.setLightNavigationBar(isColorLight)
        }
    }

    private fun pickSystemBarColor(systemBar: SystemBar, @AttrRes defaultColorAttr: Int): Pair<Int, Boolean> {
        val color = when (systemBar) {
            is SystemBar.Default -> MaterialColors.getColor(this, defaultColorAttr, Color.TRANSPARENT)
            is SystemBar.Transparent -> Color.TRANSPARENT
            is SystemBar.Colored -> systemBar.color
        }
        val isColorLight = color == Color.TRANSPARENT || ColorUtil.isColorLight(color)
        return Pair(color, isColorLight)
    }

}