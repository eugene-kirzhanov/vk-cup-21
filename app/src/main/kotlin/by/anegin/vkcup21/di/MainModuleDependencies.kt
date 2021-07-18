package by.anegin.vkcup21.di

import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.AppUiController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MainModuleDependencies {

    fun provideAppNavigator(): AppNavigator

    fun provideAppUiController(): AppUiController

}