package by.anegin.vkcup21.features.news.di

import android.content.Context
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.di.RootModule
import by.anegin.vkcup21.features.news.ui.feed.NewsFragment
import by.anegin.vkcup21.features.news.ui.login.LoginFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        MainModuleDependencies::class
    ],
    modules = [
        RootModule::class,
        NewsFeatureModule::class,
        VkModule::class
    ]
)
internal interface NewsComponent {

    fun injectNewsFragment(fragment: NewsFragment)

    fun injectLoginFragment(fragment: LoginFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun mainModuleDependencies(dependencies: MainModuleDependencies): Builder
        fun build(): NewsComponent
    }

}