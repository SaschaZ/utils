package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

interface IProcessingParams {
    val type: ParallelProcessingType
    val channelCapacity: Int
    val scope: CoroutineScope
    val numParallel: Int
    val parallelIdx: Int
    val mutex: Mutex?

    operator fun component1() = type
    operator fun component2() = channelCapacity
    operator fun component3() = scope
    operator fun component4() = numParallel
    operator fun component5() = parallelIdx
    operator fun component6() = mutex

    val parallelIndices
        get() = (0 until numParallel)
}

data class ProcessingParams(
    override val type: ParallelProcessingType = ParallelProcessingType.UNIQUE,
    override val channelCapacity: Int = Channel.BUFFERED,
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val numParallel: Int = 8,
    override val parallelIdx: Int = IPipeValue.NO_PARALLEL_EXECUTION,
    override val mutex: Mutex? = null
) : IProcessingParams

enum class ParallelProcessingType {
    UNIQUE,
    SAME,
    NONE
}

fun IProcessingParams.withParallelIdx(idx: Int) =
    ProcessingParams(type, channelCapacity, scope, numParallel, idx, mutex)