package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object MapboxUtil {

    suspend fun <R> makeCall(
        enqueue: (callback: Callback<R>) -> Unit,
        cancel: () -> Unit
    ) = suspendCancellableCoroutine<R> { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback<R> {
            override fun onResponse(call: Call<R>, response: Response<R>) {
                response.body()?.let { body ->
                    continuation.resume(body)
                } ?: run {
                    continuation.resumeWithException(IOException("Empty response body"))
                }
            }

            override fun onFailure(call: Call<R>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

}