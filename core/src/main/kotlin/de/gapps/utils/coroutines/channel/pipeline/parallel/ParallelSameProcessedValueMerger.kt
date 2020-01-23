package de.gapps.utils.coroutines.channel.pipeline.parallel

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue
import de.gapps.utils.coroutines.channel.pipeline.IProcessingParams

class ParallelSameProcessedValueMerger<out T : Any>(params: IProcessingParams) :
    AbsParallelProcessedValueMerger<T>(params) {

    private val IPipeValue<T>.isFirst
        get() = (lastSend == null
                && outIdx == 0
                && parallelIdx == 0)

    private val IPipeValue<T>.isNextOfSameParallelSeries
        get() = (outIdx == lastSend?.outIdx
                && parallelIdx == lastSend?.parallelIdx?.let { it + 1 })

    private val IPipeValue<T>.isFirstOfNextParallelSeries
        get() = (lastSend?.parallelIdx == params.numParallel - 1
                && previousOutIdx == lastSend?.outIdx
                && parallelIdx == 0)

    override val IPipeValue<@UnsafeVariance T>.isNext: Boolean
        get() = isFirst || isNextOfSameParallelSeries || isFirstOfNextParallelSeries
}