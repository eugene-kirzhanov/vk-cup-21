package by.anegin.vkcup21.core.resources

interface ResourceProvider {

    fun getString(id: Int): String

    fun getString(id: Int, vararg formatArgs: Any?): String

}