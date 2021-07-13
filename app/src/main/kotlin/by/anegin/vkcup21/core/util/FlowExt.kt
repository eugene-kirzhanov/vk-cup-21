package by.anegin.vkcup21.core.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun <T> Flow<T>.observe(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    observer: (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        flowWithLifecycle(lifecycleOwner.lifecycle, state)
            .collect { value ->
                observer(value)
            }
    }
}