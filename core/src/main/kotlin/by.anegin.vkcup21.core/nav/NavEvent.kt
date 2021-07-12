package by.anegin.vkcup21.core.nav

sealed class NavEvent {

    abstract class Destination<D>(val direction: D) : NavEvent()

    object Up : NavEvent()

}