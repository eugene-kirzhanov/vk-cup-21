package by.anegin.vkcup21.di

import android.content.Context
import by.anegin.vkcup21.VkCupApp
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

    @IoDispatcher
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

}