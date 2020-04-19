@file:Suppress("LeakingThis")

package de.gapps.utils.coroutines.channel.parallel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.*
import de.gapps.utils.coroutines.channel.pipeline.ParallelProcessingType.*
import de.gapps.utils.misc.runEach
import de.gapps.utils.misc.runEachIndexed
import kotlinx.coroutines.channels.Channel
import kotlin.reflect.KClass

inline fun <reified I : Any, reified O : Any> parallel(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("ParallelProcessor"),
    noinline factory: (idx: Int) -> IProcessingUnit<I, O>
): ParallelProcessor<I, O> = ParallelProcessor(params, outputChannel, identity, I::class, O::class, factory)

inline fun <reified I : Any, reified O : Any> IParamsHolder.parallel(
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("ParallelProcessor"),
    noinline factory: (idx: Int) -> IProcessingUnit<I, O>
): ParallelProcessor<I, O> = ParallelProcessor(params, outputChannel, identity, I::class, O::class, factory)

inline fun <reified I : Any, reified O : Any> parallel(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("ParallelProcessor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): ParallelProcessor<I, O> = ParallelProcessor(params, outputChannel, identity, I::class, O::class) {
    processor<I, O> { block(it) }
}

inline fun <reified I : Any, reified O : Any> IParamsHolder.parallel(
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("ParallelProcessor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): ParallelProcessor<I, O> = ParallelProcessor(params, outputChannel, identity, I::class, O::class) {
    processor<I, O> { block(it) }
}

open class ParallelProcessor<out I : Any, out O : Any>(
    params: IProcessingParams,
    override var outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("ParallelProcessor"),
    inputType: KClass<I>,
    outputType: KClass<O>,
    processorFactory: (idx: Int) -> IProcessingUnit<I, O>
) : Processor<I, O>(params, outputChannel, identity, inputType, outputType, {}),
    IParallelProcessedValueMerger<O> by ParallelProcessValueMerger(params) {

    private val processors = params.parallelIndices.map { processorFactory(it).withParallelIdx(it) }
    private val inputs = params.parallelIndices.map { Channel<IPipeValue<I>>(params.channelCapacity) }
    private val outputs = processors.runEachIndexed { idx -> inputs[idx].process() }

    private val jobs = outputs.mapIndexed { idx, output ->
        params.scope.launchEx {
            for (value in output) {
                @Suppress("UNCHECKED_CAST")
                value.suspendUntilInPosition {
                    outputChannel.send(value)
                }
            }
            Log.v("finished idx=$idx")
        }
    }

    private var uniqueIdx = 0
    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) {
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