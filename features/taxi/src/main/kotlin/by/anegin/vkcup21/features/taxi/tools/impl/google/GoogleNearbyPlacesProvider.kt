package by.anegin.vkcup21.features.taxi.tools.impl.google

import android.content.Context
import by.anegin.vkcup21.di.IoDispatcher
import by.anegin.vkcup21.features.taxi.models.Place
import by.anegin.vkcup21.features.taxi.tools.NearbyPlacesProvider
import by.anegin.vkcup21.taxi.R
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private typealias GooglePlaceField = com.google.android.libraries.places.api.model.Place.Field
private typealias GooglePlaceType = com.google.android.libraries.places.api.model.Place.Type

class GoogleNearbyPlacesProvider @Inject constructor(
    context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : NearbyPlacesProvider {

    companion object {
        private val isPlacesInitialized = AtomicBoolean(false)

        private fun ensurePlacesInitialized(context: Context) {
            if (!isPlacesInitialized.getAndSet(true)) {
                Places.initialize(context, context.getString(R.string.google_api_key))
            }
        }
    }

    private val placesClient: PlacesClient

    init {
        ensurePlacesInitialized(context)
        placesClient = Places.createClient(context)
    }

    override suspend fun findNearbyPlaces(limit: Int): List<Place> = withContext(ioDispatcher) {
        val request = FindCurrentPlaceRequest.newInstance(
            listOf(
                GooglePlaceField.LAT_LNG,
                GooglePlaceField.NAME,
                GooglePlaceField.ADDRESS,
                GooglePlaceField.TYPES,
            )
        )
        try {
            val response = placesClient.findCurrentPlace(request).await()
            response.placeLikelihoods
                .sortedBy { it.likelihood }
                .filter { it.place.types?.contains(GooglePlaceType.POINT_OF_INTEREST) == true }
                .take(limit)
                .mapNotNull {
                    val latitude = it.place.latLng?.latitude
                    val longitude = it.place.latLng?.longitude
                    val title = it.place.name
                    val address = it.place.address
                    if (latitude != null && longitude != null && (!title.isNullOrBlank() || !address.isNullOrBlank())) {
                        Place(latitude, longitude, title ?: "", address ?: "")
                    } else {
                        null
                    }
                }
        } catch (e: SecurityException) {
            Timber.e(e)
            emptyList()
        }
    }

}