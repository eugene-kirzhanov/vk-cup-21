package by.anegin.vkcup21.nav

import androidx.navigation.NavDirections
import by.anegin.vkcup21.core.nav.NavEvent
import by.anegin.vkcup21.core.nav.Navigator
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ActivityScoped
class VkCupAppNavigator @Inject constructor() : AppNavigator, Navigator.Source {

    private val _navigationEvents = MutableSharedFlow<NavEvent>()
    override val navigationEvents: Flow<NavEvent> = _navigationEvents.asSharedFlow()

    private val navScope = CoroutineScope(Dispatchers.Default)

    override fun navigateTo(direction: NavDirections) {
        navScope.launch {
            _navigationEvents.emit(AppDestination(direction))
        }
    }

    override fun navigateUp() {
        navScope.launch {
            delay(20)
            _navigationEvents.emit(NavEvent.Up)
        }
    }

}