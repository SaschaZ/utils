@file:Suppress("LeakingThis")

package dev.zieger.utils.coroutines.channel.parallel

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.*
import dev.zieger.utils.coroutines.channel.pipeline.ParallelProcessingType.*
import dev.zieger.utils.misc.runEach
import dev.zieger.utils.misc.runEachIndexed
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
                value.suspendUntilInPosition {
                    (outputChannel as Channel<IPipeValue<O>>).send(value)
                }
            }
            Log.v("finished idx=$idx")
        }
    }

    private var uniqueIdx = 0
    override val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(@UnsafeVariance I) -> Unit = {
        when (params.type) {
            UNIQUE -> {
                val idx = uniqueIdx++ % params.numParallel
                inputs[idx].send(rawValue.withParallelIdx(idx, params.type))
            }
            SAME -> inputs.runEachIndexed { send(rawValue.withParallelIdx(it, params.type)) }
            NONE -> throw IllegalArgumentException("NONE is not a valid type for parallel processing")
        }
    }

    override suspend fun IProducerScope<@UnsafeVariance O>.onProcessingFinished() {
        inputs.runEach { close() }
        Log.d("parallel processing finished -> joining jobs")
        jobs.runEachIndexed { join(); Log.v("joined $it") }
        Log.d("jobs joined")
    }
}