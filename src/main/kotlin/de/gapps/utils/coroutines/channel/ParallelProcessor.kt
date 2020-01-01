package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.EQUAL
import de.gapps.utils.coroutines.channel.ParallelProcessingTypes.UNIQUE
import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.log.Log
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.base.IMillisecondHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.joinAll

interface IParallelProcessor<I, O> : IProcessor<I, O> {

    override val params: IParallelProcessingParams
    val scope: CoroutineScope
        get() = params.scope
    val processorFactory: (idx: Int) -> IProcessor<I, O>

    override fun ReceiveChannel<INodeValue<I>>.process(): ReceiveChannel<IParallelNodeValue<O>> =
        Channel<IParallelNodeValue<O>>(params.channelCapacity).also { output ->
            scope.launchEx {
                (0 until params.numParallel).map { processorFactory(it) }.also { processors ->
                    processInternal(this@process, output, processors)
                    output.close()
                }
            }
        }

    suspend fun processInternal(
        input: ReceiveChannel<INodeValue<I>>,
        output: SendChannel<IParallelNodeValue<O>>,
        processors: List<IProcessor<I, O>>
    ) {
        val internalInputs = processors.map {
            Channel<INodeValue<I>>(params.channelCapacity)
        }

        val internalOutputs = processors.mapIndexed { index, iProcessor ->
            iProcessor.run { internalInputs[index].process() }
        }

        val internalJobs = internalOutputs.mapIndexed { idx, receiveChannel ->
            scope.launchEx {
                for (value in receiveChannel)
                    output.send(ParallelNodeValue(value.inIdx, value.outIdx, value.value, value.time, idx))
            }
        }

        var uniqueIdx = 0
        for (value in input) {
            when (params.type) {
                UNIQUE -> internalInputs[uniqueIdx++ % params.numParallel].send(value)
                EQUAL -> internalInputs.forEach { it.send(value) }
            }
        }

        internalInputs.forEach { it.close() }
        Log.d("parallel processing finished -> channels closed -> waiting for jobs to join")
        internalJobs.joinAll()
        Log.d("jobs joined")
    }
}

open class ParallelProcessor<I, O>(
    override val params: IParallelProcessingParams,
    override val processorFactory: (idx: Int) -> IProcessor<I, O>
) : IParallelProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var onProcessingFinished: suspend IProcessingScope<O>.() -> Unit = {}
}

interface IParallelNodeValue<O> : INodeValue<O> {

    val parallelIdx: Int
}

data class ParallelNodeValue<O>(
    override val value: O,
    override val time: ITimeEx,
    override val inIdx: Int,
    override val outIdx: Int,
    override val parallelIdx: Int
) : IParallelNodeValue<O>, IMillisecondHolder by time {
    constructor(inIdx: Int, outIdx: Int, value: O, time: ITimeEx, parallelIdx: Int) :
            this(value, time, inIdx, outIdx, parallelIdx)
}


inline fun <T, R> Collection<T>.runEachIndexed(block: T.(index: Int) -> R): List<R> =
    mapIndexed { idx, value -> value.run { block(idx) } }

inline fun <T, R> Collection<T>.runEach(block: T.() -> R): List<R> = map { it.run(block) }

suspend inline fun <T> ReceiveChannel<T>.runEach(block: T.() -> Unit) {
    for (value in this) value.run { block() }
}