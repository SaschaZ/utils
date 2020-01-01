package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.coroutines.channel.network.NodeValue
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IProcessor<I, O> : IPipelineElement<I, O> {

    override fun ReceiveChannel<INodeValue<I>>.pipe(): ReceiveChannel<INodeValue<O>> = process()

    fun ReceiveChannel<INodeValue<I>>.process(): ReceiveChannel<INodeValue<O>>

    var onProcessingFinished: suspend IProcessingScope<O>.() -> Unit
}

open class Processor<I, O>(
    override val params: IProcessingParams = ProcessingParams(),
    protected open var block: suspend IProcessingScope<O>.(value: I) -> Unit = {}
) : IProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var onProcessingFinished: suspend IProcessingScope<O>.() -> Unit = {}

    protected val scope: CoroutineScope
        get() = params.scope

    @Suppress("LeakingThis")
    private val output by lazy { Channel<INodeValue<O>>(params.channelCapacity) }

    override fun ReceiveChannel<INodeValue<I>>.process() = scope.launchEx {
        var prevValue: INodeValue<I>? = null
        for (value in this@process) {
            buildScope(value).block(value.value)
            prevValue = value
        }
        prevValue?.let { pv -> buildScope(pv).closeOutput() }
    }.let { output as ReceiveChannel<INodeValue<O>> }

    private fun buildScope(value: INodeValue<I>) = ProcessingScope<O>(
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

    private suspend fun IProcessingScope<O>.closeOutput() {
        Log.v("processing finished")
        onProcessingFinished()
        output.close()
    }
}

interface IProcessingScope<O> {

    val inIdx: Int
    var outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage

    suspend fun send(
        value: O,
        time: ITimeEx = this.time,
        outIdx: Int = this.outIdx++
    )
}

open class ProcessingScope<O>(
    inputValue: INodeValue<*>,
    override val storage: IPipelineStorage,
    private val sender: suspend (
        value: O,
        time: ITimeEx,
        outIdx: Int
    ) -> Unit
) : IProcessingScope<O> {

    override val inIdx: Int = inputValue.outIdx
    override var outIdx: Int = 0
    override val time: ITimeEx = inputValue.time

    override suspend fun send(
        value: O,
        time: ITimeEx,
        outIdx: Int
    ) = sender(value, time, outIdx)
}