package by.anegin.vkcup21.features.taxi.di

import android.content.Context
import by.anegin.vkcup21.taxi.R
import dagger.Module
import dagger.Provides

@Module
internal class TaxiMapModule {

    @Provides
    @MapboxAccessToken
    fun provideMapboxAccessToken(context: Context): String {
        return context.getString(R.string.mapbox_access_token)
    }

    @Provides
    @GoogleApiKey
    fun provideGoogleApiKey(context: Context): String {
        return context.getString(R.string.google_api_key)
    }

}