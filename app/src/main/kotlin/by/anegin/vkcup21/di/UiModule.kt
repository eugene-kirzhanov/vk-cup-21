package by.anegin.vkcup21.di

import by.anegin.vkcup21.core.nav.Navigator
import by.anegin.vkcup21.nav.AppNavigator
import by.anegin.vkcup21.nav.VkCupAppNavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
interface UiModule {

    @Binds
    @ActivityScoped
    fun bindAppNavigator(impl: VkCupAppNavigator): AppNavigator

    @Binds
    @ActivityScoped
    fun bindNavigatorSource(impl: VkCupAppNavigator): Navigator.Source

}