package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.sync.Mutex

interface IProducer<T> : IProcessor<Any?, T> {

    fun produce(): ReceiveChannel<T>

    override fun ReceiveChannel<Any?>.process() = produce()
}

fun <T> producer(
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null,
    channelCapacity: Int = 10,
    listenValue: suspend SendChannel<T>.() -> Unit
) = object : IProducer<T> {
    override fun produce() = Channel<T>(channelCapacity).apply {
        scope.launchEx(mutex = mutex) {
            listenValue()
        }
    }
}