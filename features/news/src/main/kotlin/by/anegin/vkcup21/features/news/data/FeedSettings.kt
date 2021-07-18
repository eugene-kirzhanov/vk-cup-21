package by.anegin.vkcup21.features.news.data

internal interface FeedSettings {

    fun saveNextFrom(nextFrom: String?)

    fun getNextFrom(): String?

}