package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

interface IConsumer<I : Any> : IPipelineElement<I, Any?> {

    val inputType: KClass<@UnsafeVariance I>

    override fun ReceiveChannel<IProcessValue<I>>.pipe(): ReceiveChannel<IProcessValue<Any?>> {
        consume()
        return Channel()
    }

    fun ReceiveChannel<IProcessValue<I>>.consume(): Job

    fun onConsumingFinished() = Unit
}

open class Consumer<I : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    override val inputType: KClass<I>,
    protected open var block: (suspend IConsumerScope.(value: I) -> Unit)? = null
) : IConsumer<I> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    override fun ReceiveChannel<IProcessValue<I>>.consume() = scope.launchEx {
        val b = block ?: throw IllegalStateException("Can not process without callback set")
        for (value in this@consume) {
            ConsumerScope(value, pipeline).b(value.value)
        }
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

inline fun <reified I : Any> consumer(
    params: IProcessingParams = ProcessingParams(),
    noinline block: suspend IConsumerScope.(I) -> Unit
) = Consumer(params, I::class, block)