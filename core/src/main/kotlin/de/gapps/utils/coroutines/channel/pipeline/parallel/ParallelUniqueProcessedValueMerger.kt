package de.gapps.utils.coroutines.channel.pipeline.parallel

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue
import de.gapps.utils.coroutines.channel.pipeline.IProcessingParams


class ParallelUniqueProcessedValueMerger<out T : Any>(
    params: IProcessingParams
) : AbsParallelProcessedValueMerger<T>(params) {

    private val valuesWaitingFull
        get() = valueSenderMap.size == params.numParallel

    private val IPipeValue<T>.isNextOutIdx
        get() = previousOutIdx == lastSend?.outIdx
                && previousParallelIdx == lastSend?.parallelIdx

    override val IPipeValue<@UnsafeVariance T>.isNext
        get() = valuesWaitingFull && isNextOutIdx
}

