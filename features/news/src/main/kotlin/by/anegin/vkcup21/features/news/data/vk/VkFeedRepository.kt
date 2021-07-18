package by.anegin.vkcup21.features.news.data.vk

import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.news.data.FeedRepository
import by.anegin.vkcup21.features.news.data.models.Attachment
import by.anegin.vkcup21.features.news.data.models.Audio
import by.anegin.vkcup21.features.news.data.models.Feed
import by.anegin.vkcup21.features.news.data.models.Link
import by.anegin.vkcup21.features.news.data.models.Photo
import by.anegin.vkcup21.features.news.data.models.Post
import by.anegin.vkcup21.features.news.data.models.PostSource
import by.anegin.vkcup21.features.news.data.models.Video
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.sdk.api.base.dto.BaseOkResponse
import com.vk.sdk.api.groups.dto.GroupsGroupFull
import com.vk.sdk.api.likes.LikesService
import com.vk.sdk.api.likes.dto.LikesAddResponse
import com.vk.sdk.api.newsfeed.NewsfeedService
import com.vk.sdk.api.newsfeed.dto.NewsfeedGetRecommendedResponse
import com.vk.sdk.api.newsfeed.dto.NewsfeedNewsfeedItem
import com.vk.sdk.api.photos.dto.PhotosPhoto
import com.vk.sdk.api.users.dto.UsersUserFull
import com.vk.sdk.api.wall.dto.WallWallpostAttachmentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class VkFeedRepository @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FeedRepository {

    override suspend fun getRecommendedFeed(count: Int, startFrom: String?): Feed = withContext(ioDispatcher) {
        suspendCoroutine { continuation ->
            val request = NewsfeedService().newsfeedGetRecommended(startFrom = startFrom, count = count)
            VK.execute(
                request,
                object : VKApiCallback<NewsfeedGetRecommendedResponse> {
                    override fun success(result: NewsfeedGetRecommendedResponse) {
                        val feed = parseFeedResponse(result)
                        if (feed != null) {
                            continuation.resume(feed)
                        } else {
                            continuation.resumeWithException(IOException("Invalid API response"))
                        }
                    }

                    override fun fail(error: Exception) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        }
    }

    override suspend fun likePost(post: Post): Unit = withContext(ioDispatcher) {
        suspendCoroutine { continuation ->
            val request = LikesService().likesAdd(type = "post", ownerId = post.source.id, itemId = post.id)
            VK.execute(
                request,
                object : VKApiCallback<LikesAddResponse> {
                    override fun success(result: LikesAddResponse) {
                        continuation.resume(Unit)
                    }

                    override fun fail(error: Exception) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        }
    }

    override suspend fun skipPost(post: Post): Unit = withContext(ioDispatcher) {
        suspendCoroutine { continuation ->
            val request = NewsfeedService().newsfeedIgnoreItem(type = "wall", ownerId = post.source.id, itemId = post.id)
            VK.execute(
                request,
                object : VKApiCallback<BaseOkResponse> {
                    override fun success(result: BaseOkResponse) {
                        continuation.resume(Unit)
                    }

                    override fun fail(error: Exception) {
                        continuation.resumeWithException(error)
                    }
                }
            )
        }
    }

    private fun parseFeedResponse(response: NewsfeedGetRecommendedResponse): Feed? {
        return try {
            val nextFrom = response.nextFrom
                ?: return null

            val profiles = parseProfiles(response.profiles)
            val groups = parseGroups(response.groups)
            val posts = parsePosts(response.items, profiles, groups)

            Feed(posts, nextFrom)

        } catch (t: Throwable) {
            Timber.w("Error parsing news feed response")
            null
        }
    }

    private fun parseProfiles(responseProfiles: List<UsersUserFull>?): Map<Int, PostSource> {
        val profiles = ArrayList<PostSource>()

        responseProfiles?.let { users ->
            for (user in users) {
                val userId = user.id ?: continue

                val fullName = buildString {
                    if (!user.firstName.isNullOrBlank()) {
                        append(user.firstName)
                    }
                    if (!user.lastName.isNullOrBlank()) {
                        if (isNotEmpty()) append(' ')
                        append(user.lastName)
                    }
                }

                val photoUrls = findUserPhotoUrls(user)
                val photo = if (photoUrls != null) Photo(photoUrls) else null

                profiles.add(PostSource(userId, fullName, user.screenName, photo))

            }
        }
        return profiles.map { it.id to it }.toMap()
    }

    private fun parseGroups(responseGroups: List<GroupsGroupFull>?): Map<Int, PostSource> {
        val groups = ArrayList<PostSource>()
        responseGroups?.let { groupsFull ->
            for (group in groupsFull) {
                val groupId = group.id ?: continue
                val photoUrls = findGroupPhotoUrls(group)
                val photo = if (photoUrls != null) Photo(photoUrls) else null
                groups.add(PostSource(-groupId, group.name, group.screenName, photo)) // group.id must be negative
            }
        }
        return groups.map { it.id to it }.toMap()
    }

    private fun parsePosts(responseItems: List<NewsfeedNewsfeedItem>?, profiles: Map<Int, PostSource>, groups: Map<Int, PostSource>): List<Post> {
        val posts = ArrayList<Post>()
        responseItems?.let { items ->
            for (item in items) {
                if (item is NewsfeedNewsfeedItem.NewsfeedItemWallpost) {
                    val postId = item.postId ?: continue
                    if (postId == 0) continue

                    val postSourceId = item.sourceId ?: 0
                    val postDate = item.date?.toLong() ?: 0
                    val postText = item.text

                    val postAttachments = ArrayList<Attachment>()
                    item.attachments?.forEach { attach ->
                        when (attach.type) {
                            WallWallpostAttachmentType.PHOTO -> {
                                val photoUrls = findPhotoUrls(attach.photo)
                                if (photoUrls != null) {
                                    postAttachments.add(Photo(photoUrls))
                                }
                            }
                            WallWallpostAttachmentType.VIDEO -> {
                                val videoId = attach.video?.id ?: 0
                                val videoOwnerId = attach.video?.ownerId ?: 0
                                if (videoId != 0 && videoOwnerId != 0) {
                                    val title = attach.video?.title
                                    postAttachments.add(Video(videoId, videoOwnerId, title, null))
                                }
                            }
                            WallWallpostAttachmentType.AUDIO -> {
                                val audioId = attach.audio?.id ?: 0
                                val audioOwnerId = attach.audio?.ownerId ?: 0
                                if (audioId != 0 && audioOwnerId != 0) {
                                    val artist = attach.audio?.artist
                                    val title = attach.audio?.title
                                    postAttachments.add(Audio(audioId, audioOwnerId, artist, title))
                                }
                            }
                            WallWallpostAttachmentType.LINK -> {
                                val linkUrl = attach.link?.url
                                if (!linkUrl.isNullOrEmpty()) {
                                    postAttachments.add(Link(linkUrl))
                                }
                            }
                            else -> Unit
                        }
                    }

                    // find post source
                    val postSource = (if (postSourceId > 0) profiles[postSourceId] else groups[postSourceId])
                        ?: PostSource(postSourceId, null, null, null)

                    posts.add(Post(postId, postDate, postText, postAttachments, postSource))
                }
            }
        }
        return posts
    }

    private fun findUserPhotoUrls(user: UsersUserFull?): Map<String, String>? {
        val photoUrls = HashMap<String, String>()
        // photo50, photo100
        user?.apply {
            photo?.let { photoUrls["photo"] = it }
            photo50?.let { photoUrls["photo50"] = it }
            photo100?.let { photoUrls["photo100"] = it }
            photo200?.let { photoUrls["photo200"] = it }
            photo400?.let { photoUrls["photo400"] = it }
            photoMedium?.let { photoUrls["photoMedium"] = it }
            photoBig?.let { photoUrls["photoBig"] = it }
            photoMax?.let { photoUrls["photoMax"] = it }
        }
        return if (photoUrls.isNotEmpty()) photoUrls else null
    }

    private fun findGroupPhotoUrls(group: GroupsGroupFull?): Map<String, String>? {
        val photoUrls = HashMap<String, String>()
        // photo50, photo100, photo200
        group?.apply {
            photo50?.let { photoUrls["photo50"] = it }
            photo100?.let { photoUrls["photo100"] = it }
            photo200?.let { photoUrls["photo200"] = it }
        }
        return if (photoUrls.isNotEmpty()) photoUrls else null
    }

    private fun findPhotoUrls(photo: PhotosPhoto?): Map<String, String>? {
        val photoUrls = HashMap<String, String>()
        photo?.sizes?.forEach {
            photoUrls["photo${it.height}"] = it.url
        }
        return if (photoUrls.isNotEmpty()) photoUrls else null
    }

}
