package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.IProcessValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

interface IProducer<out T : Any> : IPipelineElement<Any?, T> {

    val outputType: KClass<@UnsafeVariance T>

    override fun ReceiveChannel<IProcessValue<Any?>>.pipe(): ReceiveChannel<IProcessValue<T>> = produce()

    fun produce(): ReceiveChannel<IProcessValue<T>>

    fun onProducingFinished() = Unit
}

open class Producer<out T : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    override val outputType: KClass<@UnsafeVariance T>,
    protected open var block: (suspend IProducerScope<@UnsafeVariance T>.() -> Unit)? = null
) : IProducer<T> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    private val output = Channel<IProcessValue<@UnsafeVariance T>>(params.channelCapacity)

    override fun produce() = scope.launchEx {
        val b = block ?: throw IllegalStateException("Can not produce without callback set")
        ProducerScope<T>(pipeline, { output.close() }) { result, time, idx, isLastSend ->
            output.send(ProcessValue(NO_IDX, idx, result, time))
            if (isLastSend) {
                output.close()
                onProducingFinished()
            }
        }.b()
    }.let { output as ReceiveChannel<IProcessValue<T>> }
}

interface IProducerScope<out T> {

    var outIdx: Int
    val storage: IPipelineStorage

    suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx = TimeEx(),
        outIdx: Int = this.outIdx++,
        isLastSend: Boolean = false
    )

    fun close()
}

open class ProducerScope<out T>(
    override val storage: IPipelineStorage,
    private val closer: () -> Unit,
    private val sender: suspend (value: T, time: ITimeEx, outIdx: Int, isLastSend: Boolean) -> Unit
) : IProducerScope<T> {

    override var outIdx: Int = 0

    override suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx,
        outIdx: Int,
        isLastSend: Boolean
    ) = sender(value, time, outIdx, isLastSend)

    override fun close() = closer()
}

inline fun <reified T : Any> producer(
    params: IProcessingParams = ProcessingParams(),
    noinline block: suspend IProducerScope<T>.() -> Unit
) = Producer(
    params,
    T::class,
    block
)