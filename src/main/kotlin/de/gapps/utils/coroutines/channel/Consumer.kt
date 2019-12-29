package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IConsumer<out I> : IPipelineElement<I, Any?> {

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IProcessValue<Any?>> {
        consume()
        return Channel()
    }

    fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.consume(): Job
}

open class Consumer<out I>(
    override val params: IProcessingParams = ProcessingParams(),
    open var onConsumingFinished: () -> Unit,
    protected open val block: (suspend IConsumerScope.(value: @UnsafeVariance I) -> Unit)? = null
) : IConsumer<I> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.consume() = scope.launchEx {
        val b = block ?: throw IllegalStateException("Can not process without callback set")
        for (value in this@consume) {
            ConsumerScope(value, pipeline).b(value.value)
        }
        Log.v("consuming finished")
        onConsumingFinished()
    }
}

interface IConsumerScope {

    val inIdx: Int
    val outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage
}

open class ConsumerScope(
    value: IProcessValue<*>,
    override val storage: IPipelineStorage
) : IConsumerScope {

    override val inIdx: Int = value.inIdx
    override val outIdx: Int = value.outIdx
    override val time: ITimeEx = value.time
}