package by.anegin.vkcup21.core.nav

import kotlinx.coroutines.flow.Flow

interface AppUiController {

    fun setupSystemBars(
        statusBar: SystemBar? = null,
        navigationBar: SystemBar? = null
    )

}

internal interface AppUiControllerSource {

    val statusBar: Flow<SystemBar>

    val navigationBar: Flow<SystemBar>

}

sealed class SystemBar {

    object Default : SystemBar()

    object Transparent : SystemBar()

    class Colored(val color: Int) : SystemBar()

}