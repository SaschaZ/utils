package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.sync.Mutex

interface IConsumer<out T> : IProcessor<T, Any> {

    fun ReceiveChannel<@UnsafeVariance T>.consume()

    override fun ReceiveChannel<@UnsafeVariance T>.process(): ReceiveChannel<Any> {
        consume()
        return Channel()
    }
}

fun <T> consumer(
    scope: CoroutineScope = DefaultCoroutineScope(),
    mutex: Mutex? = null,
    listenValue: suspend (T) -> Unit
) = object : IConsumer<T> {
    override fun ReceiveChannel<T>.consume() {
        scope.launchEx(mutex = mutex) {
            for (r in this@consume) listenValue(r)
        }
    }
}