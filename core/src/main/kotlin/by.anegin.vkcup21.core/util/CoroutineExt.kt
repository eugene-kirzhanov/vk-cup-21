package by.anegin.vkcup21.core.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun <T> CoroutineScope.subscribe(
    receiveChannel: ReceiveChannel<T>,
    context: CoroutineContext = coroutineContext,
    collector: suspend (T) -> Unit
): Job {
    return launch(context) {
        receiveChannel
            .receiveAsFlow()
            .collect {
                collector(it)
            }
    }
}