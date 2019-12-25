package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.coroutineContext

suspend inline fun <I> ReceiveChannel<I>.forEach(block: (I) -> Unit) {
    for (v in this) if (!coroutineContext.isActive) return else block(v)
}

suspend fun <I, O> ReceiveChannel<I>.map(
    capacity: Int = Channel.RENDEZVOUS,
    mapping: suspend (I) -> O
) = Channel<O>(capacity).apply { launchEx { for (i in this@map) send(mapping(i)) } }

suspend fun <I, O> ReceiveChannel<I>.mapPrev(
    capacity: Int = Channel.RENDEZVOUS,
    mapping: suspend (I, I) -> O
) {
    var prev: I? = null
    Channel<O>(capacity).apply {
        launchEx {
            for (i in this@mapPrev) {
                send(mapping(prev ?: i, i))
                prev = i
            }
        }
    }
}

fun <T> ReceiveChannel<T>.clone(
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null
): Pair<ReceiveChannel<T>, ReceiveChannel<T>> =
    Pair(Channel<T>(), Channel<T>()).also { o ->
        scope.launchEx(mutex = mutex) {
            for (v in this@clone) {
                launchEx { o.first.send(v) }
                launchEx { o.second.send(v) }
            }
            o.first.close()
            o.second.close()
        }
    }

