package by.anegin.vkcup21.features.news.di

import android.content.Context
import by.anegin.vkcup21.news.R
import dagger.Module
import dagger.Provides

@Module
internal class VkModule {

    @Provides
    @VkAppId
    fun provideVkAppId(context: Context): String {
        return context.resources.getString(R.string.com_vk_sdk_AppId)
    }

}