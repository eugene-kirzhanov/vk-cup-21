package by.anegin.vkcup21.features.news.data.impl

import android.content.Context
import androidx.core.content.edit
import by.anegin.vkcup21.features.news.data.FeedSettings
import javax.inject.Inject

internal class SharedPrefsFeedSettings @Inject constructor(
    context: Context
) : FeedSettings {

    companion object {
        private const val PREFS_NAME = "feed_settings"
        private const val PREF_NEXT_FROM = "next_from"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveNextFrom(nextFrom: String?) {
        prefs.edit {
            nextFrom?.let { putString(PREF_NEXT_FROM, it) }
                ?: remove(PREF_NEXT_FROM)
        }
    }

    override fun getNextFrom(): String? {
        return prefs.getString(PREF_NEXT_FROM, null)
    }

}