package by.anegin.vkcup21.features.news.di

import android.content.Context
import by.anegin.vkcup21.di.MainModuleDependencies
import by.anegin.vkcup21.di.RootModule
import by.anegin.vkcup21.features.news.ui.NewsFragment
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        MainModuleDependencies::class
    ],
    modules = [
        RootModule::class,
        NewsFeatureModule::class
    ]
)
internal interface NewsComponent {

    fun injectNewsFragment(fragment: NewsFragment)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun mainModuleDependencies(dependencies: MainModuleDependencies): Builder
        fun build(): NewsComponent
    }

}