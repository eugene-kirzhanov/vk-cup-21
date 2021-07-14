package by.anegin.vkcup21.internal.nav

import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.AppUiControllerSource
import by.anegin.vkcup21.core.nav.SystemBar
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class HostAppUiController @Inject constructor() : AppUiController, AppUiControllerSource {

    private val _statusBar = MutableSharedFlow<SystemBar>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val statusBar = _statusBar.asSharedFlow()

    private val _navigationBar = MutableSharedFlow<SystemBar>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val navigationBar = _navigationBar.asSharedFlow()

    override fun setupSystemBars(statusBar: SystemBar?, navigationBar: SystemBar?) {
        statusBar?.let {
            _statusBar.tryEmit(it)
        }
        navigationBar?.let {
            _navigationBar.tryEmit(it)
        }
    }

}