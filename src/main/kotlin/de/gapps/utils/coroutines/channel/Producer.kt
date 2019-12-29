package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.IProcessValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IProducer<out T> : IPipelineElement<Any?, T> {

    override fun ReceiveChannel<IProcessValue<Any?>>.pipe(): ReceiveChannel<IProcessValue<T>> = produce()

    fun produce(): ReceiveChannel<IProcessValue<T>>

    fun onProducingFinished() = Unit
}

open class Producer<out T>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open var block: (suspend IProducerScope<@UnsafeVariance T>.() -> Unit)? = null
) : IProducer<T> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    private val output = Channel<IProcessValue<@UnsafeVariance T>>(params.channelCapacity)

    override fun produce() = scope.launchEx {
        val b = block ?: throw IllegalStateException("Can not produce without callback set")
        ProducerScope<T>(pipeline, { output.close(); onProducingFinished() }) { result, time, idx, isLastSend ->
            output.send(ProcessValue(NO_IDX, idx, result, time))
            if (isLastSend) {
                output.close()
                Log.v("producing finished")
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

    override fun close() {
        Log.v("producing finished")
        closer()
    }
}