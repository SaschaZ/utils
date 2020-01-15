package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

interface IProcessor<out I : Any, out O : Any> : IPipelineElement<I, O> {

    val inOutRelation: ProcessorValueRelation
    var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IPipeValue<O>> = process()

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>>

    suspend fun IProducerScope<@UnsafeVariance O>.onProcessingFinished() = Unit
}

open class Processor<out I : Any, out O : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    override val inOutRelation: ProcessorValueRelation = ProcessorValueRelation.Unspecified,
    override var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    protected open val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit =
        { throw IllegalArgumentException("No processor block defined") }
) : IProcessor<I, O>, Identity by Id("Processor") {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    private var outIdx = 0
    @Suppress("LeakingThis", "UNCHECKED_CAST")
    private fun producerScope(inIdx: Int) =
        ProducerScope<O>(inIdx, outIdx++, params.parallelIdx, pipeline, { closeOutput() }) {
            (outputChannel as SendChannel<IPipeValue<O>>).send(it)
        }

    private fun processingScope(rawValue: IPipeValue<I>) = ProcessingScope(rawValue, producerScope(rawValue.outIdx))

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process() = scope.launch {
        var prevValue: IPipeValue<I>? = null
        for (value in this@process) {
            processingScope(value).block(value.value)
            prevValue = value
        }
        Log.v("processing finished")
        prevValue?.let { pv -> processingScope(pv).closeOutput() }
    }.let { outputChannel as ReceiveChannel<IPipeValue<O>> }

    private suspend fun IProducerScope<O>.closeOutput() {
        Log.v("processing finished")
        onProcessingFinished()
        outputChannel.close()
    }
}

interface IProcessingScope<out I : Any, out O : Any> : IConsumerScope<I>, IProducerScope<O>

@Suppress("LeakingThis")
open class ProcessingScope<out I : Any, out O : Any>(
    override val rawValue: IPipeValue<I>,
    producerScope: ProducerScope<O>
) : IProcessingScope<I, O>, IProducerScope<O> by producerScope {

    override val value: I = rawValue.value
    override val time: ITimeEx = rawValue.time
}