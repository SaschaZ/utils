package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex

fun <K : Any, T : Any> ReceiveChannel<T>.forkSync(processors: Map<K, IProcessor<T, Any>>): ReceiveChannel<Map<K, Any?>> =
    SyncChannelFork(processors).run { fork() }

private class SyncChannelFork<K, T>(
    private val processors: Map<K, IProcessor<T, Any>>,
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    private val mutex: Mutex? = null
) {

    fun ReceiveChannel<T>.fork() = Channel<Map<K, Any?>>().also { it.processProcessors(cloneInput()) }

    private fun SendChannel<Map<K, Any?>>.processProcessors(channelClones: List<ReceiveChannel<T>>) {
        scope.run {
            launchEx(mutex = mutex) {
                val processorList = processors.entries.toList()
                val processorOutputs = channelClones.mapIndexedNotNull { idx, c ->
                    processorList.getOrNull(idx)?.let { it.key to it.value.run { c.process() } }
                }

                while (isActive) send(processorOutputs.mapNotNull {
                    it.second.receive()?.let { r -> it.first to r }
                }.toMap())
            }
        }
    }

    private fun ReceiveChannel<T>.cloneInput(): List<ReceiveChannel<T>> {
        val channelClones = processors.map { Channel<T>() }
        scope.apply {
            launchEx(mutex = mutex) {
                for (value in this@cloneInput) channelClones.forEach { it.send(value) }
            }
        }
        return channelClones
    }
}