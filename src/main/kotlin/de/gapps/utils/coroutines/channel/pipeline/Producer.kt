package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.coroutines.channel.network.INodeValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.network.NodeValue
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface IProducer<out T : Any> : IPipelineElement<Any?, T> {

    override fun ReceiveChannel<INodeValue<Any?>>.pipe(): ReceiveChannel<INodeValue<T>> = produce()

    fun produce(): ReceiveChannel<INodeValue<T>>

    suspend fun IProducerScope<@UnsafeVariance T>.onProducingFinished() = Unit
}

open class Producer<out T : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open val block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit = {}
) : IProducer<T> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    @Suppress("LeakingThis")
    private val output = Channel<INodeValue<T>>(params.channelCapacity)

    override fun produce() = scope.launch {
        ProducerScope<T>(
            pipeline,
            { closeOutput() }) { result, time, idx ->
            output.send(
                NodeValue(
                    NO_IDX,
                    idx,
                    result,
                    time
                )
            )
        }.block()
    }.let { output as ReceiveChannel<INodeValue<T>> }

    private suspend fun IProducerScope<T>.closeOutput() {
        delay(1.seconds)
        Log.v("producing finished")
        onProducingFinished()
        output.close()
    }
}

interface IProducerScope<out T : Any> {

    var outIdx: Int
    val storage: IPipelineStorage

    suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx = TimeEx(),
        outIdx: Int = this.outIdx++
    )

    suspend fun close()
}

open class ProducerScope<out T : Any>(
    override val storage: IPipelineStorage,
    private val closer: suspend IProducerScope<T>.() -> Unit,
    private val sender: suspend (value: T, time: ITimeEx, outIdx: Int) -> Unit
) : IProducerScope<T> {

    override var outIdx: Int = 0
    private val mutex: Mutex = Mutex()

    override suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx,
        outIdx: Int
    ) = mutex.withLock { sender(value, time, outIdx) }

    override suspend fun close() {
        Log.v("producing finished")
        closer()
    }
}