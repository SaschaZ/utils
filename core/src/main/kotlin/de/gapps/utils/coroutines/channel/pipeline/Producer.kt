package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_PARALLEL_EXECUTION
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface IProducer<out T : Any> : IPipelineElement<Any?, T> {

    override fun ReceiveChannel<IPipeValue<Any?>>.pipe(): ReceiveChannel<IPipeValue<T>> = produce()

    fun produce(): ReceiveChannel<IPipeValue<@UnsafeVariance T>>

    suspend fun IProducerScope<@UnsafeVariance T>.onProducingFinished() = Unit

    val block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit
}

open class Producer<out T : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    private val outputChannel: Channel<IPipeValue<@UnsafeVariance T>> = Channel(params.channelCapacity),
    override val block: suspend IProducerScope<@UnsafeVariance T>.() -> Unit =
        { throw IllegalArgumentException("No producer block defined") }
) : IProducer<T>, Identity by Id("Producer") {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    private val producerScope
        get() = ProducerScope<T>(pipeline, params.parallelIdx, params.type, { closeOutput() }) {
            pipeline.tick(this@Producer, PipelineElementStage.SEND_OUTPUT)
            outputChannel.send(it)
            pipeline.tick(this@Producer, PipelineElementStage.PROCESSING)
        }

    override fun produce(): ReceiveChannel<IPipeValue<T>> = scope.launch {
        pipeline.tick(this@Producer, PipelineElementStage.PROCESSING)
        producerScope.block()
    }.let { outputChannel }

    private suspend fun IProducerScope<T>.closeOutput() {
        Log.v("producing finished")
        pipeline.tick(this@Producer, PipelineElementStage.FINISHED_PROCESSING)
        onProducingFinished()
        outputChannel.close()
        pipeline.tick(this@Producer, PipelineElementStage.FINISHED_CLOSING)
    }
}

interface IProducerScope<out T : Any> {

    val inIdx: Int
    var outIdx: Int
    val parallelIdx: Int
    val parallelType: ParallelProcessingType
    val storage: IPipelineStorage

    suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx = TimeEx(),
        outIdx: Int = this.outIdx++,
        parallelIdx: Int = NO_PARALLEL_EXECUTION
    ) = send(PipeValue(value, time, inIdx, outIdx, parallelIdx))

    suspend fun send(rawValue: IPipeValue<@UnsafeVariance T>): IPipeValue<@UnsafeVariance T>

    suspend fun close()
}

open class ProducerScope<out T : Any>(
    override val inIdx: Int,
    override var outIdx: Int,
    override val parallelIdx: Int = NO_PARALLEL_EXECUTION,
    override val parallelType: ParallelProcessingType = ParallelProcessingType.NONE,
    override val storage: IPipelineStorage,
    private val closer: suspend IProducerScope<T>.() -> Unit,
    private val sender: suspend (rawValue: IPipeValue<T>) -> Unit
) : IProducerScope<T> {

    constructor(
        storage: IPipelineStorage,
        parallelIdx: Int = NO_PARALLEL_EXECUTION,
        parallelType: ParallelProcessingType = ParallelProcessingType.NONE,
        closer: suspend IProducerScope<T>.() -> Unit,
        sender: suspend (rawValue: IPipeValue<T>) -> Unit
    ) : this(NO_IDX, 0, parallelIdx, parallelType, storage, closer, sender)

    private val mutex: Mutex = Mutex()

    override suspend fun send(
        rawValue: IPipeValue<@UnsafeVariance T>
    ) = mutex.withLock { rawValue.withParallelIdx(parallelIdx, parallelType).apply { sender(this) } }

    override suspend fun close() {
        Log.v("producing finished")
        closer()
    }
}