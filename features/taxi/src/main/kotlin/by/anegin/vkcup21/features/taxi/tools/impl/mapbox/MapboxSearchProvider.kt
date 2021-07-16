package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import android.app.Application
import android.content.Context
import by.anegin.vkcup21.features.taxi.tools.SearchProvider
import by.anegin.vkcup21.taxi.R
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.location.DefaultLocationProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapboxSearchProvider @Inject constructor(
    context: Context
) : SearchProvider {

    init {
        (context.applicationContext as Application).apply {
            MapboxSearchSdk.initialize(
                this,
                context.getString(R.string.mapbox_access_token),
                DefaultLocationProvider(this)
            )
        }
    }

    fun search() {
//        val categorySearchEngine = MapboxSearchSdk.createCategorySearchEngine()
//
//        MapboxSearchSdk.createReverseGeocodingSearchEngine().search()

    }

}