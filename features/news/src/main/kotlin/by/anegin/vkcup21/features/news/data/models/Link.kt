package by.anegin.vkcup21.features.news.data.models

internal class Link(
    private val url: String
) : Attachment {

    fun getUrl() = url

}