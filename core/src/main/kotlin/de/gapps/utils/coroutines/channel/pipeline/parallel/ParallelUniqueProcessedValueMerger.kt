package de.gapps.utils.coroutines.channel.pipeline.parallel

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue
import de.gapps.utils.coroutines.channel.pipeline.IProcessingParams
import de.gapps.utils.log.logV


class ParallelUniqueProcessedValueMerger<out T : Any>(
    params: IProcessingParams
) : AbsParallelProcessedValueMerger<T>(params) {

    private val valuesWaitingFull
        get() = valueSenderMap.size == params.numParallel

    private val IPipeValue<T>.isFirst
        get() = lastSend == null
                && inIdx == 0
                && outIdx == 0
                && parallelIdx == 0

    private val IPipeValue<T>.isNextOutIdxOfSameSeries
        get() = previousOutIdx == lastSend?.outIdx
                && previousParallelIdx == lastSend?.parallelIdx

    private val IPipeValue<T>.isNextOutOfNextSeries
        get() = lastSend?.parallelIdx == params.numParallel - 1
                && parallelIdx == 0
                && outIdx - 1 == lastSend?.outIdx
                && inIdx - 1 == lastSend?.inIdx

    override val IPipeValue<@UnsafeVariance T>.isNext
        get() = (isFirst || isNextOutIdxOfSameSeries || isNextOutOfNextSeries) logV { "$this -> $it" }
}

