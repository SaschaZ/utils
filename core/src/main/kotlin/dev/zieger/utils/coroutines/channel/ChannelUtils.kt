package dev.zieger.utils.coroutines.channel

import dev.zieger.utils.coroutines.builder.launchEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend inline fun <I> ReceiveChannel<I>.forEach(block: (I) -> Unit) {
    for (v in this) if (!coroutineContext.isActive) return else block(v)
}

suspend fun <I, O> ReceiveChannel<I>.map(
    scope: CoroutineScope,
    capacity: Int = Channel.RENDEZVOUS,
    mapping: suspend (I) -> O
) = Channel<O>(capacity).apply { scope.launchEx { for (i in this@map) send(mapping(i)) } }

suspend fun <I, O> ReceiveChannel<I>.mapPrev(
    scope: CoroutineScope,
    capacity: Int = Channel.RENDEZVOUS,
    mapping: suspend (I, I) -> O
) {
    var prev: I? = null
    Channel<O>(capacity).apply {
        scope.launchEx {
            for (i in this@mapPrev) {
                send(mapping(prev ?: i, i))
                prev = i
            }
        }
    }
}

