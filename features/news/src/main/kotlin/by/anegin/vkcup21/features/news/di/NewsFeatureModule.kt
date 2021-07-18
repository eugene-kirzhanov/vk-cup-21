package by.anegin.vkcup21.features.news.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.anegin.vkcup21.di.ViewModelFactory
import by.anegin.vkcup21.di.ViewModelKey
import by.anegin.vkcup21.features.news.data.FeedRepository
import by.anegin.vkcup21.features.news.data.vk.VkFeedRepository
import by.anegin.vkcup21.features.news.ui.feed.NewsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
@Suppress("unused")
internal interface NewsFeatureModule {

    @Binds
    fun bindFeedRepository(impl: VkFeedRepository): FeedRepository

    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    fun newsViewModel(viewModel: NewsViewModel): ViewModel

}