package by.anegin.vkcup21.core.resources

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidResourceProvider @Inject constructor(
    @ApplicationContext context: Context
) : ResourceProvider {

    private val resources = context.resources

    override fun getString(id: Int): String {
        return resources.getString(id)
    }

    override fun getString(id: Int, vararg formatArgs: Any?): String {
        return resources.getString(id, *formatArgs)
    }

}