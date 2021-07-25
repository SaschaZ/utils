package dev.zieger.utils.misc

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger


var DEFAULT_NUM_PARALLEL = 16

fun <I, O> Flow<I>.mapParallel(
    numParallel: Int = DEFAULT_NUM_PARALLEL,
    transform: suspend (I) -> O
): Flow<O> =
    mapParallel(
        numParallel,
        CoroutineScope(Executors.newFixedThreadPool(numParallel + 1).asCoroutineDispatcher() + SupervisorJob()),
        transform
    )

fun <I, O> Flow<I>.mapParallel(
    numParallel: Int = DEFAULT_NUM_PARALLEL,
    scope: CoroutineScope,
    transform: suspend (I) -> O
): Flow<O> = flow {
    val output = Channel<O>(Channel.RENDEZVOUS)
    val finished = AtomicInteger(0)
    val channels = (0 until numParallel).map {
        Channel<I>(Channel.RENDEZVOUS).also { channel ->
            scope.launch {
                for (value in channel) output.send(transform(value))
                if (finished.incrementAndGet() == numParallel)
                    output.close()
            }
        }
    }

    scope.launch {
        var idx = 0
        collect { value ->
            channels[idx++ % channels.size].send(value)
        }
        channels.forEach { it.close() }
    }

    emitAll(output)
    scope.cancel()
}