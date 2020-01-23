@file:Suppress("unused")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_PARALLEL_EXECUTION
import de.gapps.utils.misc.nullWhenZero
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx


interface IPipeValue<out T> : Comparable<IPipeValue<@UnsafeVariance T>> {

    companion object {

        const val NO_IDX = -1
        const val NO_PARALLEL_EXECUTION = -1
    }

    val value: T
    val time: ITimeEx
    val inIdx: Int
    val outIdx: Int
    val parallelIdx: Int
    val parallelType: ParallelProcessingType

    override fun compareTo(other: IPipeValue<@UnsafeVariance T>): Int {
        return when (parallelType) {
            ParallelProcessingType.SAME -> {
                inIdx.compareTo(other.inIdx).nullWhenZero()
                    ?: outIdx.compareTo(other.outIdx).nullWhenZero()
                    ?: parallelIdx.compareTo(other.parallelIdx).nullWhenZero()
                    ?: time.compareTo(other.time)
            }
            ParallelProcessingType.UNIQUE -> {
                outIdx.compareTo(other.outIdx).nullWhenZero()
                    ?: parallelIdx.compareTo(other.parallelIdx).nullWhenZero()
                    ?: inIdx.compareTo(other.inIdx).nullWhenZero()
                    ?: time.compareTo(other.time)
            }
            ParallelProcessingType.NONE -> time.compareTo(other.time)
        }
    }

    val wasProcessedParallel
        get() = parallelIdx > NO_PARALLEL_EXECUTION
}

data class PipeValue<T>(
    override val value: T,
    override val time: ITimeEx = TimeEx(),
    override val inIdx: Int = NO_IDX,
    override val outIdx: Int = NO_IDX,
    override val parallelIdx: Int = NO_PARALLEL_EXECUTION,
    override val parallelType: ParallelProcessingType = ParallelProcessingType.NONE
) : IPipeValue<T> {

    constructor(
        rawValue: IPipeValue<T>,
        parallelIdx: Int = NO_PARALLEL_EXECUTION,
        parallelType: ParallelProcessingType = ParallelProcessingType.NONE
    ) : this(rawValue.value, rawValue.time, rawValue.inIdx, rawValue.outIdx, parallelIdx, parallelType)
}

fun <T : Any> IPipeValue<T>.withParallelIdx(idx: Int, type: ParallelProcessingType) =
    PipeValue(value, time, inIdx, outIdx, idx, type)