package by.anegin.vkcup21.features.taxi.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class MapboxAccessToken

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class GoogleApiKey
