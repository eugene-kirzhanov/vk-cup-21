package by.anegin.vkcup21.features.news.ui.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import by.anegin.vkcup21.features.news.data.models.Attachment
import by.anegin.vkcup21.features.news.data.models.Audio
import by.anegin.vkcup21.features.news.data.models.Link
import by.anegin.vkcup21.features.news.data.models.Photo
import by.anegin.vkcup21.features.news.data.models.Video
import by.anegin.vkcup21.news.R
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

internal class AttachmentsPagerAdapter(
    private val attachments: List<Attachment>,
    private val glide: RequestManager
) : PagerAdapter() {

    override fun getCount() = attachments.size

    override fun isViewFromObject(view: View, obj: Any) = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val inflater = LayoutInflater.from(container.context)
        val itemView = inflater.inflate(R.layout.item_attachment, container, false)
        val imagePhoto = itemView.findViewById<ImageView>(R.id.imagePhoto)
        val imageIcon = itemView.findViewById<ImageView>(R.id.imageIcon)
        container.addView(itemView)

        when (val attachment = attachments[position]) {

            is Photo -> {
                val photoUrl = attachment.getUrl("photo1280", "photo807", "photo604")
                if (photoUrl != null) {
                    glide
                        .load(photoUrl)
                        .apply(RequestOptions.centerCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagePhoto)
                    imageIcon.setImageDrawable(null)
                } else {
                    glide.clear(imagePhoto)
                    imageIcon.setImageResource(R.drawable.ic_no_photo)
                }
            }

            is Video -> {
                val photoUrl = attachment.getPhotoUrl("photo1280", "photo807", "photo604")
                if (photoUrl != null) {
                    glide
                        .load(photoUrl)
                        .apply(RequestOptions.centerCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imagePhoto)
                    imageIcon.setImageDrawable(null)
                } else {
                    glide.clear(imagePhoto)
                    imageIcon.setImageResource(R.drawable.ic_no_video)
                }
            }

            is Audio -> {
                glide.clear(imagePhoto)
                imageIcon.setImageResource(R.drawable.ic_audio)
            }

            is Link -> {
                glide.clear(imagePhoto)
                imageIcon.setImageResource(R.drawable.ic_link)
            }
        }

        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        try {
            container.removeView(obj as View)
        } catch (ignored: Exception) {
        }
    }

}