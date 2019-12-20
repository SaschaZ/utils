package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface IProcessor<in I, out O> {

    fun ReceiveChannel<I>.process(): ReceiveChannel<O?>
}

fun <I, O> processor(
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null,
    channelCapacity: Int = Channel.BUFFERED,
    processValue: suspend SendChannel<O>.(I) -> Unit
) = object : IProcessor<I, O> {
    override fun ReceiveChannel<I>.process() = Channel<O>(channelCapacity).apply {
        scope.launchEx {
            for (value in this@process) mutex?.withLock { processValue(value) } ?: processValue(value)
        }
    }
}