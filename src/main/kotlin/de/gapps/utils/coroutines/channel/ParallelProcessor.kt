package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.EQUAL
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.UNIQUE
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.base.IMillisecondHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

interface IParallelProcessor<out I, out O> : IProcessor<I, O> {

    override val params: IParallelProcessingParams
    val scope: CoroutineScope
        get() = params.scope
    val processorFactory: (idx: Int) -> IProcessor<I, O>

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.process(): ReceiveChannel<IParallelProcessValue<O>> =
        Channel<IParallelProcessValue<O>>(params.channelCapacity).also { output ->
            scope.launchEx {
                (0 until params.numParallel).map { processorFactory(it) }.also { processors ->
                    processInternal(this@process, output, processors)
                    output.close()
                }
            }
        }

    suspend fun processInternal(
        input: ReceiveChannel<IProcessValue<@UnsafeVariance I>>,
        output: SendChannel<IParallelProcessValue<@UnsafeVariance O>>,
        processors: List<IProcessor<@UnsafeVariance I, @UnsafeVariance O>>
    ) {
        val internalInputs = processors.runEach {
            Channel<IProcessValue<I>>(params.channelCapacity)
        }
        val internalOutputs = processors.runEachIndexed { internalInputs[it].process() }
        val internalJobs = internalOutputs.runEachIndexed { idx ->
            scope.launchEx {
                for (value in this@runEachIndexed) {
                    output.send(ParallelProcessValue(value.inIdx, value.outIdx, value.value, value.time, idx))
                }
            }
        }

        var uniqueIdx = 0
        for (value in input) {
            when (params.type) {
                UNIQUE -> internalInputs[uniqueIdx++ % params.numParallel].run { send(value) }
                EQUAL -> internalInputs.runEach { send(value) }
            }
        }
        when (params.type) {
            UNIQUE -> internalInputs.runEach { close() }
            EQUAL -> internalInputs.runEach { close() }
        }
        Log.d("parallel processing finished -> channels closed -> waiting for jobs to join")
        internalJobs.runEach { join() }
        Log.d("jobs joined")
    }
}

open class ParallelProcessor<out I, out O>(
    override val params: IParallelProcessingParams,
    override val processorFactory: (idx: Int) -> IProcessor<I, O>
) : IParallelProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
}

interface IParallelProcessValue<out O> : IProcessValue<O> {

    val parallelIdx: Int

    operator fun component5(): Int = parallelIdx
}

data class ParallelProcessValue<out O>(
    override val inIdx: Int,
    override val outIdx: Int,
    override val value: O,
    override val time: ITimeEx,
    override val parallelIdx: Int
) : IParallelProcessValue<O>, IMillisecondHolder by time


inline fun <T, R> Collection<T>.runEachIndexed(block: T.(index: Int) -> R): List<R> =
    mapIndexed { idx, value -> value.run { block(idx) } }

inline fun <T, R> Collection<T>.runEach(block: T.() -> R): List<R> = map { it.run(block) }

suspend inline fun <T> ReceiveChannel<T>.runEach(block: T.() -> Unit) {
    for (value in this) value.run { block() }
}