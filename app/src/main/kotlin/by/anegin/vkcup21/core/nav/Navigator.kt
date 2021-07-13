package by.anegin.vkcup21.core.nav

import kotlinx.coroutines.flow.Flow

interface Navigator<D> {

    interface Source {
        val navigationEvents: Flow<NavEvent>
    }

    fun navigateTo(direction: D)

    fun navigateUp()

}