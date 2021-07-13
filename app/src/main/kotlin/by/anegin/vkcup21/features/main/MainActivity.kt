package by.anegin.vkcup21.features.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.fragment.NavHostFragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.core.nav.AppDestination
import by.anegin.vkcup21.core.nav.NavEvent
import by.anegin.vkcup21.core.nav.Navigator
import by.anegin.vkcup21.core.util.observe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var navigatorSource: Navigator.Source

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        DynamicNavHostFragment

        navigatorSource.navigationEvents.observe(this) { event ->
            when (event) {
                is AppDestination -> navController.navigate(event.direction)
                NavEvent.Up -> navController.navigateUp()
                else -> Unit
            }
        }
    }

}