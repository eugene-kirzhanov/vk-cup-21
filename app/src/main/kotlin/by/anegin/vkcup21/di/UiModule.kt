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
abstract class UiModule {

    @Binds
    internal abstract fun bindAppNavigator(impl: HostAppNavigator): AppNavigator

    @Binds
    internal abstract fun bindAppNavigatorSource(impl: HostAppNavigator): AppNavigatorSource

    @Binds
    internal abstract fun bindAppUiController(impl: HostAppUiController): AppUiController

    @Binds
    internal abstract fun bindAppUiControllerSource(impl: HostAppUiController): AppUiControllerSource

}