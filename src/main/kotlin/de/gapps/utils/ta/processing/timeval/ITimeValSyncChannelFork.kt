package de.gapps.utils.ta.processing.timeval

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.time.values.ITimeVal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex

fun <K : Any, T : Any> ReceiveChannel<ITimeVal<T>>.forkSync(processors: Map<K, ITimeValProcessor<T, Any>>)
        : ReceiveChannel<Map<K, ITimeVal<Any?>>> = ITimeValSyncChannelFork(processors).run { process() }

private class ITimeValSyncChannelFork<K, T>(
    private val processors: Map<K, ITimeValProcessor<T, Any>>,
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    private val mutex: Mutex? = null
) : IProcessor<ITimeVal<T>, Map<K, ITimeVal<Any?>>> {

    override fun ReceiveChannel<ITimeVal<T>>.process() =
        Channel<Map<K, ITimeVal<Any?>>>().also { it.processProcessors(cloneInput()) }

    private fun SendChannel<Map<K, ITimeVal<Any?>>>.processProcessors(channelClones: List<ReceiveChannel<ITimeVal<T>>>) {
        scope.run {
            launchEx(mutex = mutex) {
                val processorList = processors.entries.toList()
                val processorOutputs = channelClones.mapIndexedNotNull { idx, c ->
                    processorList.getOrNull(idx)?.let { it.key to it.value.run { c.process() } }
                }

                while (isActive) send(processorOutputs.mapNotNull {
                    it.second.receive().let { r -> it.first to r }
                }.toMap())
            }
        }
    }

    private fun ReceiveChannel<ITimeVal<T>>.cloneInput(): List<ReceiveChannel<ITimeVal<T>>> {
        val channelClones = processors.map { Channel<ITimeVal<T>>() }
        scope.apply {
            launchEx(mutex = mutex) {
                for (value in this@cloneInput) channelClones.forEach { it.send(value) }
            }
        }
        return channelClones
    }
}