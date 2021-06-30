@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.coroutines.flow

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.forEach
import dev.zieger.utils.misc.catch
import dev.zieger.utils.misc.min
import dev.zieger.utils.misc.whenNotNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.coroutineContext

suspend fun <T : Comparable<T>> Flow<T>.fuseComparable(other: Flow<T>, bufferSize: Int = 3): Flow<T> =
    fuseComparable(other, CoroutineScope(coroutineContext), bufferSize)

fun <T : Comparable<T>> Flow<T>.fuseComparable(other: Flow<T>, scope: CoroutineScope, bufferSize: Int = 3): Flow<T> =
    fuse(other, scope, bufferSize) { v0, v1 -> min(v0, v1) }

suspend fun <T : Any> Flow<T>.fuse(other: Flow<T>, bufferSize: Int = 3, block: (T, T) -> T): Flow<T> =
    fuse(other, CoroutineScope(coroutineContext), bufferSize, block)

fun <T : Any> Flow<T>.fuse(other: Flow<T>, scope: CoroutineScope, bufferSize: Int = 3, block: (T, T) -> T): Flow<T> {
    val i0 = Channel<T>(bufferSize)
    val i1 = Channel<T>(bufferSize)
    val o = Channel<T>(bufferSize)

    fun Flow<T>.collectAndSend(channel: Channel<T>) = scope.launchEx {
        collect { channel.send(it) }
        channel.close()
    }

    collectAndSend(i0)
    other.collectAndSend(i1)

    scope.launchEx {
        var var0: T? = null
        var var1: T? = null
        do {
            var0 = var0 ?: i0.receiveSafe()
            var1 = var1 ?: i1.receiveSafe()
            whenNotNull(var0, var1) { v0, v1 -> block(v0, v1) }.also {
                when (it) {
                    null -> {
                        when {
                            var0 == null && var1 != null -> {
                                o.send(var1!!)
                                i1.forEach { c -> o.send(c) }
                                var1 = null
                            }
                            var1 == null && var0 != null -> {
                                o.send(var0!!)
                                i0.forEach { c -> o.send(c) }
                                var0 = null
                            }
                        }
                    }
                    var0 -> var0 = null
                    var1 -> var1 = null
                }
            }?.also { o.send(it) }
        } while (var0 != null || var1 != null)
        o.close()
    }

    return flow { emitAll(o) }
}

@Suppress("EXPERIMENTAL_API_USAGE")
private suspend fun <T : Any> Channel<T>.receiveSafe(): T? = catch(null) { if (isClosedForReceive) null else receive() }
