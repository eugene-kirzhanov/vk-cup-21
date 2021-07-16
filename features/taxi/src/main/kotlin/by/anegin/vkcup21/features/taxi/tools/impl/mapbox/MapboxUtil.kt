package by.anegin.vkcup21.features.taxi.tools.impl.mapbox

import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchCallback
import com.mapbox.search.SearchRequestTask
import com.mapbox.search.result.SearchResult
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <R> makeSuspendCall(
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

suspend fun makeSuspendSearch(
    search: (callback: SearchCallback) -> SearchRequestTask
) = suspendCancellableCoroutine<List<SearchResult>> { continuation ->
    val task = search(
        object : SearchCallback {
            override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                continuation.resume(results)
            }

            override fun onError(e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    )
    continuation.invokeOnCancellation {
        task.cancel()
    }
}
