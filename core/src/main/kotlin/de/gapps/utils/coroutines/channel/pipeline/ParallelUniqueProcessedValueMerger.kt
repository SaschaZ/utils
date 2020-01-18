package de.gapps.utils.coroutines.channel.pipeline


class ParallelUniqueProcessedValueMerger<out T : Any>(
    params: IProcessingParams
) : AbsParallelProcessedValueMerger<T>(params) {

    private val valuesWaitingFull
        get() = valueSenderList.size == params.numParallel

    private val IPipeValue<T>.isNextOutIdx
        get() = previousOutIdx == lastSend?.outIdx
                && previousParallelIdx == lastSend?.parallelIdx

    private val IPipeValue<T>.isSmallest
        get() = if (valuesWaitingFull) valueSenderList.minBy { it.first.outIdx } == this else false

    private val IPipeValue<T>.isSmallestParallel
        get() = isSmallest || if (valuesWaitingFull)
            valueSenderList.groupBy { it.first.outIdx }.minBy { it.key }
                ?.value?.minBy { it.first.parallelIdx } == this
        else false

    override val IPipeValue<@UnsafeVariance T>.canSend
        get() = isNextOutIdx || isSmallestParallel
}

