package by.anegin.vkcup21.features.news.data

import by.anegin.vkcup21.features.news.data.models.Feed
import by.anegin.vkcup21.features.news.data.models.Post

internal interface FeedRepository {

    suspend fun getRecommendedFeed(count: Int, startFrom: String? = null): Feed

    suspend fun likePost(post: Post)

    suspend fun skipPost(post: Post)

}