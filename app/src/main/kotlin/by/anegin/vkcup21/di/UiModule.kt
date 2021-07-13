package by.anegin.vkcup21.di

import by.anegin.vkcup21.core.nav.AppNavigator
import by.anegin.vkcup21.core.nav.Navigator
import by.anegin.vkcup21.core.nav.VkCupAppNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface UiModule {

    @Binds
    fun bindAppNavigator(impl: VkCupAppNavigator): AppNavigator

    @Binds
    fun bindNavigatorSource(impl: VkCupAppNavigator): Navigator.Source

}