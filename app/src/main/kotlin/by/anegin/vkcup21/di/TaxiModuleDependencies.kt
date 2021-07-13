package by.anegin.vkcup21.di

import by.anegin.vkcup21.core.nav.AppNavigator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaxiModuleDependencies {

    fun provideAppNavigator(): AppNavigator

}