package by.anegin.vkcup21.core.nav

import androidx.navigation.NavDirections
import kotlinx.coroutines.flow.Flow

interface AppNavigator {

    fun navigateTo(direction: NavDirections)

    fun navigateUp()

}

internal interface AppNavigatorSource {

    val navigationEvents: Flow<NavEvent>

}

sealed class NavEvent {

    class Destination(val direction: NavDirections) : NavEvent()

    object Up : NavEvent()

}