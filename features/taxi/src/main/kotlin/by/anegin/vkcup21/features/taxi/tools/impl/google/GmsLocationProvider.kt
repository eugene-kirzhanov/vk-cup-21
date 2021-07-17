package by.anegin.vkcup21.features.taxi.tools.impl.google

import android.content.Context
import android.os.Looper
import by.anegin.vkcup21.di.ApplicationScope
import by.anegin.vkcup21.features.taxi.models.Position
import by.anegin.vkcup21.features.taxi.tools.LocationProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class GmsLocationProvider @Inject constructor(
    context: Context,
    @ApplicationScope applicationScope: CoroutineScope
) : LocationProvider {

    private val locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 5 * 1000
    }

    override val location: Flow<Position> = callbackFlow {

        // try to load last location
        val lastLocationJob = async {
            try {
                locationProviderClient.lastLocation.await()?.let { lastLocation ->
                    trySend(Position(lastLocation.latitude, lastLocation.longitude))
                }
            } catch (e: SecurityException) {
                Timber.w("Can't get last location due to SecurityException: $e")
            }
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                result ?: return

                // cancel last location loading
                lastLocationJob.cancel()

                // emit last known location to flow
                result.locations.lastOrNull()?.let { lastLocation ->
                    trySend(Position(lastLocation.latitude, lastLocation.longitude))
                }
            }
        }

        var isSubscribedToLocationUpdates = false
        do {
            try {
                locationProviderClient
                    .requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
                    .addOnFailureListener { e ->
                        close(e)
                    }
                isSubscribedToLocationUpdates = true
            } catch (e: SecurityException) {
                Timber.w("Can't get subscribe to location updates due to SecurityException: $e")

                // wait some time in hope that permission will be granted
                delay(1000)
            }
        } while (isActive && !isSubscribedToLocationUpdates)

        awaitClose {
            lastLocationJob.cancel()

            if (isSubscribedToLocationUpdates) {
                locationProviderClient.removeLocationUpdates(callback)
            }
        }

    }.shareIn(
        applicationScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )

}