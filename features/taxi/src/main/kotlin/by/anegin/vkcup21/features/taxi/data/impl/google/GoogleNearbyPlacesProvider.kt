package by.anegin.vkcup21.features.taxi.data.impl.google

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.data.NearbyPlacesProvider
import by.anegin.vkcup21.features.taxi.data.models.Place
import by.anegin.vkcup21.features.taxi.di.GoogleApiKey
import by.anegin.vkcup21.features.taxi.util.LocationUtil
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private typealias GooglePlaceField = com.google.android.libraries.places.api.model.Place.Field
private typealias GooglePlaceType = com.google.android.libraries.places.api.model.Place.Type

internal class GoogleNearbyPlacesProvider @Inject constructor(
    private val context: Context,
    @GoogleApiKey private val googleApiKey: String,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NearbyPlacesProvider {

    companion object {
        private val isPlacesInitialized = AtomicBoolean(false)

        private fun ensurePlacesInitialized(context: Context, googleApiKey: String) {
            if (!isPlacesInitialized.getAndSet(true)) {
                Places.initialize(context, googleApiKey)
            }
        }
    }

    override suspend fun findNearbyPlaces(limit: Int): List<Place> = withContext(ioDispatcher) {
        if (LocationUtil.isLocationPermissionGranted(context)) {
            try {
                ensurePlacesInitialized(context, googleApiKey)

                val request = FindCurrentPlaceRequest.newInstance(
                    listOf(
                        GooglePlaceField.LAT_LNG,
                        GooglePlaceField.NAME,
                        GooglePlaceField.ADDRESS,
                        GooglePlaceField.TYPES,
                    )
                )

                val placesClient = Places.createClient(context)
                val response = placesClient.findCurrentPlace(request).await()

                return@withContext response.placeLikelihoods
                    .sortedBy { it.likelihood }
                    .filter { it.place.types?.contains(GooglePlaceType.POINT_OF_INTEREST) == true }
                    .take(limit)
                    .mapNotNull {
                        val position = it.place.latLng?.toPosition()
                        val title = it.place.name
                        val address = it.place.address
                        if (position != null && (!title.isNullOrBlank() || !address.isNullOrBlank())) {
                            Place(
                                position = position,
                                title = title ?: "",
                                address = address ?: ""
                            )
                        } else {
                            null
                        }
                    }
            } catch (e: SecurityException) {
                Timber.e("Error finding nearby places: ${e.message}")
            }
        }
        emptyList()
    }

}