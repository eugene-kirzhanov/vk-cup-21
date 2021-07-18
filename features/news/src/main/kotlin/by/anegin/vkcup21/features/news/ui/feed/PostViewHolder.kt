package by.anegin.vkcup21.features.news.ui.feed

import android.animation.ValueAnimator
import android.view.View
import by.anegin.vkcup21.features.news.data.models.Attachment
import by.anegin.vkcup21.features.news.data.models.Audio
import by.anegin.vkcup21.features.news.data.models.Link
import by.anegin.vkcup21.features.news.data.models.Photo
import by.anegin.vkcup21.features.news.data.models.Post
import by.anegin.vkcup21.features.news.data.models.PostSource
import by.anegin.vkcup21.features.news.data.models.Video
import by.anegin.vkcup21.features.news.ui.feed.view.ExpandableTextView
import by.anegin.vkcup21.features.news.ui.feed.view.LinePageIndicator
import by.anegin.vkcup21.features.news.util.DateUtils
import by.anegin.vkcup21.news.R
import by.anegin.vkcup21.news.databinding.ItemPostBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

internal class PostViewHolder(
    private val itemView: View,
    private val glide: RequestManager,
    private val onExpandStateChangeListener: (Boolean, Boolean) -> Unit, // (isExpanded, isAnimating)
    private val onProfileClickListener: (PostSource?) -> Unit, // (postSource)
    private val onOpenAttachmentListener: (Attachment?) -> Unit // (attachment)
) {

    private val binding = ItemPostBinding.bind(itemView)

    private var likeAnimator: ValueAnimator? = null
    private var likeAnimatorEndValue: Float? = null
    private var skipAnimator: ValueAnimator? = null
    private var skipAnimatorEndValue: Float? = null

    private var currentPostId = 0
    private var currentAttachment: Attachment? = null

    init {
        reset()
        binding.textPost.setListener(object : ExpandableTextView.Listener {
            override fun onAnimationStarted(expanding: Boolean) {
                onExpandStateChangeListener(!expanding, true)
            }

            override fun onStateChanged(expanded: Boolean) {
                onExpandStateChangeListener(expanded, false)
            }
        })
        binding.buttonOpenAttachment.setOnClickListener {
            onOpenAttachmentListener(currentAttachment)
        }
    }

    fun bind(post: Post?) {
        if (post == null) {
            currentPostId = 0
            itemView.visibility = View.GONE
            return
        }
        itemView.visibility = View.VISIBLE

        if (post.id == currentPostId) return
        currentPostId = post.id

        // profile/group avatar
        if (post.source.photo != null) {
            val url = post.source.photo.getUrl("photo100", "photo50", "photo200")
            glide
                .load(url)
                .apply(
                    RequestOptions
                        .circleCropTransform()
                        .error(R.drawable.no_avatar)
                )
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageAvatar)
        } else {
            glide
                .load(R.drawable.no_avatar)
                .apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.imageAvatar)
        }
        binding.imageAvatar.setOnClickListener {
            onProfileClickListener(post.source)
        }

        // profile/group name
        binding.textName.text = post.source.name ?: "..."
        binding.textName.setOnClickListener {
            onProfileClickListener(post.source)
        }

        // post time
        binding.textTime.text = DateUtils.formatDateTime(binding.textTime.context, post.time)

        // post text
        binding.textPost.setText(post.text)
        binding.textPost.setOnClickListener {
            binding.textPost.toggle()
        }

        // photos
        val attachments = if (!post.attachments.isNullOrEmpty()) {
            post.attachments
        } else {
            listOf(Photo(emptyMap()))
        }
        binding.viewPagerPhotos.adapter = AttachmentsPagerAdapter(attachments, glide)

        if (attachments.size > 1) {
            binding.buttonPrevPhoto.setOnClickListener {
                val currentItem = binding.indicatorPhotos.getCurrentItem()
                if (currentItem > 0) {
                    binding.indicatorPhotos.setCurrentItem(currentItem - 1)
                }
            }
            binding.buttonNextPhoto.setOnClickListener {
                val currentItem = binding.indicatorPhotos.getCurrentItem()
                if (currentItem < attachments.size - 1) {
                    binding.indicatorPhotos.setCurrentItem(currentItem + 1)
                }
            }

            binding.indicatorPhotos.visibility = View.VISIBLE
            binding.indicatorPhotos.setViewPager(binding.viewPagerPhotos)
            binding.indicatorPhotos.setCurrentItem(0)

            binding.indicatorPhotos.setListener(object : LinePageIndicator.Listener {
                override fun onPageChanged(position: Int) {
                    updateAttachmentInfo(attachments[position])
                }
            })

        } else {
            binding.buttonPrevPhoto.setOnClickListener(null)
            binding.buttonNextPhoto.setOnClickListener(null)
            binding.buttonPrevPhoto.isClickable = false
            binding.buttonNextPhoto.isClickable = false

            binding.indicatorPhotos.visibility = View.GONE
            binding.indicatorPhotos.setViewPager(null)
            binding.indicatorPhotos.setListener(null)
        }

        updateAttachmentInfo(attachments[0])
    }

    private fun updateAttachmentInfo(attachment: Attachment) {
        currentAttachment = attachment
        when (attachment) {
            is Photo -> {
                binding.buttonOpenAttachment.visibility = View.GONE
                binding.imageAttachmentIcon.visibility = View.GONE
                binding.textAttachmentInfo.visibility = View.GONE
            }
            is Video -> {
                binding.buttonOpenAttachment.visibility = View.VISIBLE
                binding.imageAttachmentIcon.visibility = View.VISIBLE
                binding.textAttachmentInfo.visibility = View.VISIBLE
                binding.imageAttachmentIcon.setImageResource(R.drawable.ic_play_video)

                var text = binding.textAttachmentInfo.context.getString(R.string.press_to_watch_video)
                if (!attachment.title.isNullOrBlank()) {
                    text += "\n" + attachment.title
                }
                binding.textAttachmentInfo.text = text
            }
            is Audio -> {
                binding.buttonOpenAttachment.visibility = View.VISIBLE
                binding.imageAttachmentIcon.visibility = View.VISIBLE
                binding.textAttachmentInfo.visibility = View.VISIBLE
                binding.imageAttachmentIcon.setImageResource(R.drawable.ic_play_audio)

                var text = binding.textAttachmentInfo.context.getString(R.string.press_to_play_audio)
                val audioTitle = attachment.getFullTitle()
                if (audioTitle.isNotBlank()) {
                    text += "\n" + audioTitle
                }
                binding.textAttachmentInfo.text = text
            }
            is Link -> {
                binding.buttonOpenAttachment.visibility = View.VISIBLE
                binding.imageAttachmentIcon.visibility = View.VISIBLE
                binding.textAttachmentInfo.visibility = View.VISIBLE
                binding.imageAttachmentIcon.setImageResource(R.drawable.ic_open_link)
                binding.textAttachmentInfo.setText(R.string.press_to_open_link)
            }
        }
    }

    fun reset() {
        likeAnimatorEndValue = null
        likeAnimator?.cancel()
        skipAnimatorEndValue = null
        skipAnimator?.cancel()

        binding.badgeLike.alpha = 0f
        binding.badgeSkip.alpha = 0f

        binding.textPost.setExpanded(expand = false, animate = false)
    }

    fun onSwipe(value: Float) {

        var likeAlpha = 0f
        var skipAlpha = 0f
        if (value > 0f) {
            likeAlpha = 1f
            skipAlpha = 0f
        } else if (value < 0f) {
            likeAlpha = 0f
            skipAlpha = 1f
        }

        if (likeAnimatorEndValue != likeAlpha) {
            likeAnimatorEndValue = likeAlpha
            likeAnimator?.cancel()
            likeAnimator = makeAlphaAnimation(binding.badgeLike, likeAlpha)
            likeAnimator?.start()
        }

        if (skipAnimatorEndValue != skipAlpha) {
            skipAnimatorEndValue = skipAlpha
            skipAnimator?.cancel()
            skipAnimator = makeAlphaAnimation(binding.badgeSkip, skipAlpha)
            skipAnimator?.start()
        }

    }

    private fun makeAlphaAnimation(view: View, toAlpha: Float): ValueAnimator {
        val anim = ValueAnimator.ofFloat(view.alpha, toAlpha)
        anim.duration = if (toAlpha > view.alpha) 250L else 150L
        anim.addUpdateListener {
            view.alpha = it.animatedValue as Float
        }
        return anim
    }

}