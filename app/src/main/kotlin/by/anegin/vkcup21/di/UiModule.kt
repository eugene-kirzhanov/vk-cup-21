package by.anegin.vkcup21.di

import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppNavigatorSource
import by.anegin.vkcup21.core.nav.AppUiController
import by.anegin.vkcup21.core.nav.AppUiControllerSource
import by.anegin.vkcup21.internal.nav.HostAppNavigator
import by.anegin.vkcup21.internal.nav.HostAppUiController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
internal interface UiModule {

    @Binds
    fun bindAppNavigator(impl: HostAppNavigator): AppNavigator

    @Binds
    fun bindAppNavigatorSource(impl: HostAppNavigator): AppNavigatorSource

    @Binds
    fun bindAppUiController(impl: HostAppUiController): AppUiController

    @Binds
    fun bindAppUiControllerSource(impl: HostAppUiController): AppUiControllerSource

}