package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.coroutines.channel.pipeline.ParallelProcessingTypes.EQUAL
import de.gapps.utils.coroutines.channel.pipeline.ParallelProcessingTypes.UNIQUE
import de.gapps.utils.log.Log
import de.gapps.utils.misc.runEach
import de.gapps.utils.misc.runEachIndexed
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.base.IMillisecondHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

interface IParallelProcessor<out I : Any, out O : Any> : IProcessor<I, O> {

    override val params: IParallelProcessingParams
    val scope: CoroutineScope
        get() = params.scope
    val processorFactory: (idx: Int) -> IProcessor<I, O>

    override fun ReceiveChannel<INodeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IParallelNodeValue<O>> =
        Channel<IParallelNodeValue<O>>(params.channelCapacity).also { output ->
            scope.launch {
                (0 until params.numParallel).map { processorFactory(it) }.also { processors ->
                    processInternal(this@process, output, processors)
                    output.close()
                }
            }
        }

    suspend fun processInternal(
        input: ReceiveChannel<INodeValue<@UnsafeVariance I>>,
        output: SendChannel<IParallelNodeValue<@UnsafeVariance O>>,
        processors: List<IProcessor<@UnsafeVariance I, @UnsafeVariance O>>
    ) {
        val internalInputs: List<Channel<INodeValue<I>>> =
            processors.map { Channel<INodeValue<I>>(params.channelCapacity) }

        val internalOutputs: List<ReceiveChannel<INodeValue<O>>> =
            processors.runEachIndexed { index -> internalInputs[index].process() }

        val internalJobs: List<Job> = internalOutputs.mapIndexed { idx, receiveChannel ->
            scope.launch {
                for (value in receiveChannel)
                    output.send(
                        ParallelNodeValue(
                            value.inIdx,
                            value.outIdx,
                            value.value,
                            value.time,
                            idx
                        )
                    )
            }
        }

        var uniqueIdx = 0
        for (value in input) {
            when (params.type) {
                UNIQUE -> internalInputs[uniqueIdx++ % params.numParallel].send(value)
                EQUAL -> internalInputs.forEach { it.send(value) }
            }
        }

        internalInputs.runEach { close() }
        Log.d("parallel processing finished -> channels closed -> waiting for jobs to join")
        internalJobs.joinAll()
        Log.d("jobs joined")
    }
}

open class ParallelProcessor<out I : Any, out O : Any>(
    override val params: IParallelProcessingParams,
    override val processorFactory: (idx: Int) -> IProcessor<I, O>
) : IParallelProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
}

interface IParallelNodeValue<out O : Any> : INodeValue<O> {

    val parallelIdx: Int
}

data class ParallelNodeValue<out O : Any>(
    override val value: O,
    override val time: ITimeEx,
    override val inIdx: Int,
    override val outIdx: Int,
    override val parallelIdx: Int
) : IParallelNodeValue<O>, IMillisecondHolder by time {
    constructor(inIdx: Int, outIdx: Int, value: O, time: ITimeEx, parallelIdx: Int) :
            this(value, time, inIdx, outIdx, parallelIdx)
}