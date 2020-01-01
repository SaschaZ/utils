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

interface IProducer<T> : IPipelineElement<Any?, T> {

    override fun ReceiveChannel<INodeValue<Any?>>.pipe(): ReceiveChannel<INodeValue<T>> = produce()

    fun produce(): ReceiveChannel<INodeValue<T>>

    var onProducingFinished: suspend IProducerScope<T>.() -> Unit
}

open class Producer<T>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open var block: suspend IProducerScope<T>.() -> Unit = {}
) : IProducer<T> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var onProducingFinished: suspend IProducerScope<T>.() -> Unit = {}

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

interface IProducerScope<T> {

    var outIdx: Int
    val storage: IPipelineStorage

    suspend fun send(
        value: T,
        time: ITimeEx = TimeEx(),
        outIdx: Int = this.outIdx++
    )

    suspend fun close()
}

open class ProducerScope<T>(
    override val storage: IPipelineStorage,
    private val closer: suspend IProducerScope<T>.() -> Unit,
    private val sender: suspend (value: T, time: ITimeEx, outIdx: Int) -> Unit
) : IProducerScope<T> {

    override var outIdx: Int = 0

    override suspend fun send(
        value: T,
        time: ITimeEx,
        outIdx: Int
    ) = sender(value, time, outIdx)

    override suspend fun close() {
        Log.v("producing finished")
        closer()
    }
}