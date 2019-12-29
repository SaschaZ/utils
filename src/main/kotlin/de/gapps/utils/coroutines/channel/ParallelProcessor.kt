package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.EQUAL
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.UNIQUE
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.base.IMillisecondHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.reflect.KClass

interface IParallelProcessor<out I : Any, out O : Any> : IProcessor<I, O> {

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
        val internalIo = processors.runEach {
            Channel<IProcessValue<I>>(params.channelCapacity).run { this to process() }
        }
        val internalJobs = internalIo.runEachIndexed { idx ->
            scope.launchEx {
                second.runEach {
                    output.send(ParallelProcessValue(inIdx, outIdx, value, time, idx))
                }
            }
        }

        var uniqueIdx = 0
        for (value in input) {
            when (params.type) {
                UNIQUE -> internalIo[uniqueIdx++ % params.numParallel].run { first.send(value) }
                EQUAL -> internalIo.runEach { first.send(value) }
            }
        }
        when (params.type) {
            UNIQUE -> internalIo.runEach { first.close() }
            EQUAL -> internalIo.runEach { first.close() }
        }
        internalJobs.runEach { join() }
    }
}

open class ParallelProcessor<out I : Any, out O : Any>(
    override val params: IParallelProcessingParams,
    override val inputType: KClass<@UnsafeVariance I>,
    override val outputType: KClass<@UnsafeVariance O>,
    override val processorFactory: (idx: Int) -> IProcessor<I, O>
) : IParallelProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
}

interface IParallelProcessValue<out O : Any> : IProcessValue<O> {

    val parallelIdx: Int

    operator fun component5(): Int = parallelIdx
}

data class ParallelProcessValue<out O : Any>(
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

inline fun <reified I : Any, reified O : Any> parallel(
    params: IParallelProcessingParams,
    noinline processorFactory: (Int) -> IProcessor<I, O>
) = ParallelProcessor(params, I::class, O::class, processorFactory)