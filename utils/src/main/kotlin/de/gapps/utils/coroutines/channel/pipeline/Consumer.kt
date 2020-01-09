package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

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
    protected open val block: (suspend IConsumerScope<@UnsafeVariance I>.(value: @UnsafeVariance I) -> Unit)? = null
) : IConsumer<I> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.consume() = scope.launch {
        val b = block ?: throw IllegalStateException("Can not process without callback set")
        lateinit var prevValue: IPipeValue<I>
        for (value in this@consume) {
            ConsumerScope(value, pipeline).b(value.value)
            prevValue = value
        }
        Log.v("consuming finished")
        ConsumerScope(prevValue, pipeline).onConsumingFinished()
    }
}

interface IConsumerScope<out T : Any> {

    val value: IPipeValue<T>
    val inIdx: Int
    val outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage
}

open class ConsumerScope<out T : Any>(
    override val value: IPipeValue<T>,
    override val storage: IPipelineStorage
) : IConsumerScope<T> {

    @Suppress("LeakingThis")
    override val inIdx: Int = value.inIdx
    @Suppress("LeakingThis")
    override val outIdx: Int = value.outIdx
    @Suppress("LeakingThis")
    override val time: ITimeEx = value.time
}