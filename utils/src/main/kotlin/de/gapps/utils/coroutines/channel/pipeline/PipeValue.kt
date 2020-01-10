@file:Suppress("unused")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_IDX
import de.gapps.utils.coroutines.channel.pipeline.IPipeValue.Companion.NO_PARALLEL_EXECUTION
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.IMillisecondHolder


interface IPipeValue<out T> : IMillisecondHolder {

    companion object {

        const val NO_IDX = -1
        const val NO_PARALLEL_EXECUTION = -1
    }

    val value: T
    val time: ITimeEx
    val inIdx: Int
    val outIdx: Int
    val parallelIdx: Int

    operator fun component1(): T = value
    operator fun component2(): ITimeEx = time
    operator fun component3(): Int = inIdx
    operator fun component4(): Int = outIdx
    operator fun component5(): Int = parallelIdx

    val wasProcessedParallel
        get() = parallelIdx > NO_PARALLEL_EXECUTION
}

data class PipeValue<T>(
    override val value: T,
    override val time: ITimeEx = TimeEx(),
    override val inIdx: Int = NO_IDX,
    override val outIdx: Int = NO_IDX,
    override val parallelIdx: Int = NO_PARALLEL_EXECUTION
) : IPipeValue<T>, IMillisecondHolder by time {

    constructor(
        rawValue: IPipeValue<T>,
        parallelIdx: Int = NO_PARALLEL_EXECUTION
    ) : this(rawValue.value, rawValue.time, rawValue.inIdx, rawValue.outIdx, parallelIdx)
}