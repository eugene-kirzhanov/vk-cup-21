package by.anegin.vkcup21.internal.nav

import androidx.navigation.NavDirections
import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppNavigatorSource
import by.anegin.vkcup21.core.nav.NavEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HostAppNavigator @Inject constructor() : AppNavigator, AppNavigatorSource {

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    override val navigationEvents = _navigationEvents.asSharedFlow()

    private val navScope = CoroutineScope(Dispatchers.Default)

    override fun navigateTo(direction: NavDirections) {
        navScope.launch {
            _navigationEvents.emit(NavEvent.Destination(direction))
        }
    }

    override fun navigateUp() {
        navScope.launch {
            delay(20)
            _navigationEvents.emit(NavEvent.Up)
        }
    }

}