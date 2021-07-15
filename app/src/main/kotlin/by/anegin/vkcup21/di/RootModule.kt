package by.anegin.vkcup21.di

import android.content.Context
import by.anegin.vkcup21.VkCupApp
import by.anegin.vkcup21.core.resources.AndroidResourceProvider
import by.anegin.vkcup21.core.resources.ResourceProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
class RootModule {

    @ApplicationScope
    @Provides
    fun provideApplicationScope(context: Context): CoroutineScope {
        return (context as VkCupApp).applicationScope
    }

    @Provides
    fun provideResourceProvider(context: Context): ResourceProvider {
        return AndroidResourceProvider(context)
    }

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher {
        return Dispatchers.Default
    }

}