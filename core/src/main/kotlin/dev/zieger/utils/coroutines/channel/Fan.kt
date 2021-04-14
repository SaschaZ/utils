package dev.zieger.utils.coroutines.channel

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.forEach
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


suspend fun <I, O> ReceiveChannel<I>.fan(
    num: Int = Runtime.getRuntime().availableProcessors(),
    scope: CoroutineScope = IoCoroutineScope(),
    block: suspend (I) -> O
): ReceiveChannel<O> = Channel<O>().also { output ->
    scope.launchEx {
        val indexed = indexed(scope)
        val cache = HashMap<Int, O>()
        var nextIdx = 0
        val mutex = Mutex()

        suspend fun output(idx: Int, value: O) = mutex.withLock {
            cache[idx] = value
            while (cache.containsKey(nextIdx))
                cache.remove(nextIdx++)?.let { output.send(it) }
        }

        (0 until num).map {
            scope.launchEx {
                indexed.forEach { (idx, value) ->
                    catch(Unit, maxExecutions = 10, onCatch = { delay(5.seconds) }) {
                        output(idx, block(value))
                    }
                }
            }
        }.joinAll()
        output.close()
    }
}

suspend fun <T> ReceiveChannel<T>.indexed(
    scope: CoroutineScope
): ReceiveChannel<Pair<Int, T>> {
    var idx = 0
    return map(scope) { idx++ to it }
}

suspend fun <I, O> ReceiveChannel<I>.map(
    scope: CoroutineScope,
    block: suspend (I) -> O
): ReceiveChannel<O> = Channel<O>().also { output ->
    scope.launchEx {
        forEach { output.send(block(it)) }
        output.close()
    }
}

fun <T> List<T>.toChannel(scope: CoroutineScope): ReceiveChannel<T> = Channel<T>().also { output ->
    scope.launchEx {
        forEach { output.send(it) }
        output.close()
    }
}