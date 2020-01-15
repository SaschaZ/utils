@file:Suppress("LeakingThis")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.ParallelProcessingTypes.SAME
import de.gapps.utils.coroutines.channel.pipeline.ParallelProcessingTypes.UNIQUE
import de.gapps.utils.misc.runEach
import de.gapps.utils.misc.runEachIndexed
import kotlinx.coroutines.channels.Channel

open class ParallelProcessor<out I : Any, out O : Any>(
    params: IProcessingParams,
    inOutRelation: ProcessorValueRelation = ProcessorValueRelation.Unspecified,
    override var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    processorFactory: (idx: Int) -> IProcessor<I, O> = { throw IllegalArgumentException("No processor factory defined") }
) : Processor<I, O>(params, inOutRelation, outputChannel),
    IParallelProcessedValueMerger<O> by ParallelProcessValueMerger(params) {

    private val processors = params.parallelIndices.map { processorFactory(it).withParallelIdx(it) }

    private val inputs = params.parallelIndices.map { Channel<IPipeValue<I>>(params.channelCapacity) }
    private val outputs = processors.runEachIndexed { idx -> inputs[idx].process() }

    private val jobs = outputs.mapIndexed { idx, output ->
        params.scope.launchEx {
            for (value in output) {
                @Suppress("UNCHECKED_CAST")
                value.suspendUntilInPosition { (outputChannel as Channel<IPipeValue<O>>).send(PipeValue(value, idx)) }
            }
        }
    }

    private var uniqueIdx = 0
    override val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(@UnsafeVariance I) -> Unit = {
        when (params.type) {
            UNIQUE -> inputs[uniqueIdx++ % params.numParallel].send(rawValue)
            SAME -> inputs.runEach { send(rawValue) }
        }
    }

    override suspend fun IProducerScope<@UnsafeVariance O>.onProcessingFinished() {
        inputs.runEach { close() }
        Log.d("parallel processing finished -> joining jobs")
        jobs.runEach { join() }
        Log.d("jobs joined")
    }
}