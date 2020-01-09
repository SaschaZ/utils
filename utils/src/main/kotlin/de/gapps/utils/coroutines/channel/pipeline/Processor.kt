package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface IProcessor<out I : Any, out O : Any> : IPipelineElement<I, O> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IPipeValue<O>> = process()

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>>

    suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.onProcessingFinished() = Unit
}

open class Processor<out I : Any, out O : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit = {}
) : IProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    protected val scope: CoroutineScope
        get() = params.scope

    @Suppress("LeakingThis")
    private val output by lazy { Channel<IPipeValue<O>>(params.channelCapacity) }

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process() = scope.launch {
        var prevValue: IPipeValue<I>? = null
        for (value in this@process) {
            buildScope(value).block(value.value)
            prevValue = value
        }
        prevValue?.let { pv -> buildScope(pv).closeOutput() }
    }.let { output as ReceiveChannel<IPipeValue<O>> }

    private fun buildScope(value: IPipeValue<I>) =
        ProcessingScope<I, O>(
            value,
            pipeline
        ) { result, time, idx ->
            output.send(
                PipeValue(
                    value.outIdx,
                    idx,
                    result,
                    time
                )
            )
        }

    private suspend fun IProcessingScope<I, O>.closeOutput() {
        Log.v("processing finished")
        onProcessingFinished()
        output.close()
    }
}

interface IProcessingScope<out I : Any, out O : Any> {

    val value: IPipeValue<I>
    val inIdx: Int
    var outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage

    suspend fun send(
        value: @UnsafeVariance O,
        time: ITimeEx = this.time,
        outIdx: Int = this.outIdx++
    )
}

open class ProcessingScope<out I : Any, out O : Any>(
    override val value: IPipeValue<I>,
    override val storage: IPipelineStorage,
    private val sender: suspend (
        value: O,
        time: ITimeEx,
        outIdx: Int
    ) -> Unit
) : IProcessingScope<I, O> {

    override val inIdx: Int = value.outIdx
    override var outIdx: Int = 0
    override val time: ITimeEx = value.time
    private val mutex = Mutex()

    override suspend fun send(
        value: @UnsafeVariance O,
        time: ITimeEx,
        outIdx: Int
    ) = mutex.withLock { sender(value, time, outIdx) }
}