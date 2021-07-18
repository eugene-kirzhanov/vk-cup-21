package by.anegin.vkcup21.features.news.util

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
internal class VkGLideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder
            .setMemoryCache(LruResourceCache(1024 * 1024 * 20L))
            .setBitmapPool(LruBitmapPool(1024 * 1024 * 30L))
            .setDiskCache(InternalCacheDiskCacheFactory(context, 1024 * 1024 * 100L))
            .setDefaultRequestOptions(
                RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            )
    }

}