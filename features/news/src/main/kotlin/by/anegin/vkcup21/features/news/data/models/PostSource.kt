package by.anegin.vkcup21.features.news.data.models

internal class PostSource(
    val id: Int,
    val name: String?,
    private val screenName: String?,
    val photo: Photo?
) {

    fun getSourceUrl() = "https://vk.com/$screenName"

}