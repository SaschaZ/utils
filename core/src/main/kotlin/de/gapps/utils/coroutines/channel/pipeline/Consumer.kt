package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IConsumer<out I : Any> : IPipelineElement<I, Any?> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IPipeValue<Any?>> {
        consume()
        return Channel()
    }

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.consume(): Job

    suspend fun IConsumerScope<@UnsafeVariance I>.onConsumingFinished() = Unit
}

open class Consumer<out I : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open val block: (suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit) =
        { throw IllegalArgumentException("No consumer block defined") }
) : IConsumer<I>, Identity by NoId {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.consume() = scope.launchEx {
        var prevValue: IPipeValue<I>? = null
        for (value in this@consume) {
            ConsumerScope(value, pipeline).block(value.value)
            prevValue = value
        }
        Log.v("consuming finished")
        prevValue?.let { pv -> ConsumerScope(pv, pipeline).onConsumingFinished() }
    }
}

interface IConsumerScope<out T : Any> {

    val rawValue: IPipeValue<T>
    val value: T
    val inIdx: Int
    val outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage
}

@Suppress("LeakingThis")
open class ConsumerScope<out T : Any>(
    override val rawValue: IPipeValue<T>,
    override val storage: IPipelineStorage
) : IConsumerScope<T> {

    override val value: T = rawValue.value
    override val inIdx: Int = rawValue.inIdx
    override val outIdx: Int = rawValue.outIdx
    override val time: ITimeEx = rawValue.time
}