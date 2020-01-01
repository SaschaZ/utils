package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.coroutines.channel.network.NodeValue
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

interface IProcessor<out I : Any, out O : Any> : IPipelineElement<I, O> {

    override fun ReceiveChannel<INodeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<INodeValue<O>> = process()

    fun ReceiveChannel<INodeValue<@UnsafeVariance I>>.process(): ReceiveChannel<INodeValue<O>>

    var onProcessingFinished: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.() -> Unit
}

open class Processor<out I : Any, out O : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open var block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit = {}
) : IProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var onProcessingFinished: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.() -> Unit = {}

    protected val scope: CoroutineScope
        get() = params.scope

    @Suppress("LeakingThis")
    private val output by lazy { Channel<INodeValue<O>>(params.channelCapacity) }

    override fun ReceiveChannel<INodeValue<@UnsafeVariance I>>.process() = scope.launch {
        var prevValue: INodeValue<I>? = null
        for (value in this@process) {
            buildScope(value).block(value.value)
            prevValue = value
        }
        prevValue?.let { pv -> buildScope(pv).closeOutput() }
    }.let { output as ReceiveChannel<INodeValue<O>> }

    private fun buildScope(value: INodeValue<I>) =
        ProcessingScope<I, O>(
            value,
            pipeline
        ) { result, time, idx ->
            output.send(
                NodeValue(
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

    val value: INodeValue<I>
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
    override val value: INodeValue<I>,
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

    override suspend fun send(
        value: @UnsafeVariance O,
        time: ITimeEx,
        outIdx: Int
    ) = sender(value, time, outIdx)
}