package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.logV

class ParallelSameProcessedValueMerger<out T : Any>(params: IProcessingParams) :
    AbsParallelProcessedValueMerger<T>(params) {

    private val IPipeValue<T>.isNextOfSameParallelSeries
        get() = (outIdx == lastSend?.outIdx && parallelIdx == lastSend?.parallelIdx?.let { it + 1 }
                || outIdx == 0 && parallelIdx == 0) logV { "\nisNextOfSameParallelSeries>=$it\n$this\n$lastSend" }

    private val IPipeValue<T>.isFirstOfNextParallelSeries
        get() = ((lastSend == null || lastSend?.parallelIdx == params.numParallel - 1 && previousOutIdx == lastSend?.outIdx)
                && parallelIdx == 0) logV { "\nisFirstOfNextParallelSeries>=$it\n$this\n$lastSend" }

    override val IPipeValue<@UnsafeVariance T>.canSend: Boolean
        get() = isNextOfSameParallelSeries //|| isFirstOfNextParallelSeries
}