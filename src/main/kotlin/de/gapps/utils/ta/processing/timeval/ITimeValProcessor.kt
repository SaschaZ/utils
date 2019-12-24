package de.gapps.utils.ta.processing.timeval

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.time.time
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal
import kotlinx.coroutines.channels.Channel

interface ITimeValProcessor<in I, out O> : IProcessor<ITimeVal<@UnsafeVariance I>, ITimeVal<O?>>

fun <I, O> Channel<ITimeVal<I>>.process(processValue: suspend (I) -> O) = Channel<ITimeVal<O>>().apply {
    DefaultCoroutineScope().apply {
        launchEx {
            for (value in this@process) send(
                STimeVal.TimeVal(
                    processValue(value.value),
                    value.time
                )
            )
        }
    }
}

fun <T> Channel<ITimeVal<T>>.listen(listen: suspend (T) -> Unit) = Channel<ITimeVal<T>>().apply {
    DefaultCoroutineScope().apply {
        launchEx {
            for (value in this@listen) {
                listen(value.value)
                send(value)
            }
        }
    }
}   