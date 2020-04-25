package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

interface IProcessingParams {
    val type: ParallelProcessingType
    val channelCapacity: Int
    val numParallel: Int
    val parallelIdx: Int
    val mutex: Mutex?
    val scopeFactory: () -> CoroutineScopeEx

    operator fun component1() = type
    operator fun component2() = channelCapacity
    operator fun component3() = numParallel
    operator fun component4() = parallelIdx
    operator fun component5() = mutex

    val parallelIndices
        get() = (0 until numParallel)
}

data class ProcessingParams(
    override val type: ParallelProcessingType = ParallelProcessingType.UNIQUE,
    override val channelCapacity: Int = Channel.BUFFERED,
    override val numParallel: Int = 8,
    override val parallelIdx: Int = IPipeValue.NO_PARALLEL_EXECUTION,
    override val mutex: Mutex? = null,
    override val scopeFactory: () -> CoroutineScopeEx = { DefaultCoroutineScope() }
) : IProcessingParams

enum class ParallelProcessingType {
    UNIQUE,
    SAME,
    NONE
}

fun IProcessingParams.withParallelIdx(idx: Int) =
    ProcessingParams(type, channelCapacity, numParallel, idx, mutex)