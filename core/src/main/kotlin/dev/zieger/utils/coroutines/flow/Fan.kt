package dev.zieger.utils.coroutines.flow

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.forEach
import dev.zieger.utils.coroutines.scope.IoCoroutineScope
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.AtomicInteger

suspend fun <I, O> Flow<I>.fan(
    num: Int = Runtime.getRuntime().availableProcessors(),
    scope: CoroutineScope = IoCoroutineScope(),
    block: suspend (I) -> O
): Flow<O> = flow {
    val output = Channel<Pair<Long, O>>(3)
    val input = Channel<Pair<Long, I>>(3)

    val closed = AtomicInteger(0)
    val jobs = (0 until num).map {
        scope.launchEx {
            input.forEach { (idx, value) -> output.send(idx to block(value)) }
            if (closed.incrementAndGet() == num) output.close()
        }
    }

    scope.launchEx {
        var idx = 0L
        collect { input.send(idx++ to it) }
        input.close()
    }

    val resultMap = HashMap<Long, O>()
    var nextSendIdx = 0L
    output.forEach { (idx, value) ->
        resultMap[idx] = value
        while (resultMap.minOfOrNull { it.key } == nextSendIdx)
            resultMap.remove(nextSendIdx++)?.let { emit(it) }
    }

    jobs.runEach { cancel() }
}

suspend fun <I, O> List<I>.fan(
    num: Int = Runtime.getRuntime().availableProcessors(),
    scope: CoroutineScope = IoCoroutineScope(),
    block: suspend (I) -> O
): List<O> = asFlow().fan(num, scope, block).toList()

