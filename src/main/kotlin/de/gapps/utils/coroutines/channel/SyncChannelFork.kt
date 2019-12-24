package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex

fun <K : Any, T : Any> ReceiveChannel<T>.forkSync(
    processors: Map<K, IProcessor<T, Any>>,
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null,
    channelCapacity: Int = Channel.BUFFERED
) = processor<T, Map<K, Any?>> {

    fun SendChannel<Map<K, Any?>>.processProcessors(channelClones: List<ReceiveChannel<T>>) {
        scope.run {
            launchEx(mutex = mutex) {
                val processorList = processors.entries.toList()
                val processorOutputs = channelClones.mapIndexedNotNull { idx, c ->
                    processorList.getOrNull(idx)?.let { it.key to it.value.run { c.process() } }
                }

                while (isActive) send(processorOutputs.map {
                    it.second.receive().let { r -> it.first to r }
                }.toMap())
            }
        }
    }

    fun ReceiveChannel<T>.cloneInput(): List<ReceiveChannel<T>> {
        val channelClones = processors.map { Channel<T>(channelCapacity) }
        scope.apply {
            launchEx(mutex = mutex) {
                for (value in this@cloneInput) channelClones.forEach { it.send(value) }
            }
        }
        return channelClones
    }

    processProcessors(cloneInput())
}