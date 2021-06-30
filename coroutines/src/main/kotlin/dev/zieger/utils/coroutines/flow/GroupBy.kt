@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.coroutines.flow

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.forEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext

suspend inline fun <T, K> Flow<T>.groupBy(crossinline buildKey: (T) -> K): Flow<Pair<K, Flow<T>>> = groupBy(
    CoroutineScope(coroutineContext), buildKey
)

inline fun <T, K> Flow<T>.groupBy(
    scope: CoroutineScope,
    crossinline buildKey: (T) -> K
): Flow<Pair<K, Flow<T>>> = flow {
    var currentKey: K? = null
    val output = Channel<Pair<K, Flow<T>>>(3)
    val main = Channel<Pair<K, Channel<T>>>(3).apply {
        scope.launchEx {
            forEach { (k, values) -> output.send(k to flow { emitAll(values) }) }
            output.close()
        }
    }

    scope.launchEx {
        var currentChan: Channel<T>? = null
        collect { item ->
            val key = buildKey(item)
            if (currentKey?.let { it == key } != true) {
                currentChan?.close()
                currentChan = Channel<T>(3).also { main.send(key to it) }
            }
            currentKey = key
            currentChan?.send(item)
        }
        currentChan?.close()
        main.close()
    }

    output.forEach { emit(it) }
}

operator fun <T> Flow<T>.plus(other: Flow<T>): Flow<T> =
    listOf(this, other).asFlow().flattenConcat()