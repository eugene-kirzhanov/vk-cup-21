package by.anegin.vkcup21.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import by.anegin.vkcup21.R
import by.anegin.vkcup21.core.nav.NavEvent
import by.anegin.vkcup21.core.nav.Navigator
import by.anegin.vkcup21.nav.AppDestination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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

        lifecycleScope.launch {
            navigatorSource.navigationEvents.collect {
                when (it) {
                    is AppDestination -> navController.navigate(it.direction)
                    NavEvent.Up -> navController.navigateUp()
                    else -> Unit
                }
            }
        }
    }

}