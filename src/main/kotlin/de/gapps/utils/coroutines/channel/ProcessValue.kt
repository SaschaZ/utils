package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.channel.IProcessValue.Companion.NO_IDX
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.IMillisecondHolder


interface IProcessValue<out T> : IMillisecondHolder {

    companion object {

        const val NO_IDX = -1
    }

    val inIdx: Int
    val outIdx: Int
    val value: T
    val time: ITimeEx

    val idx: Int
        get() = outIdx

    operator fun component1(): Int = inIdx
    operator fun component2(): Int = outIdx
    operator fun component3(): T = value
    operator fun component4(): ITimeEx = time
}

data class ProcessValue<out T>(
    override val inIdx: Int,
    override val outIdx: Int,
    override val value: T,
    override val time: ITimeEx = TimeEx()
) : IProcessValue<@UnsafeVariance T>, IMillisecondHolder by time {

    constructor(idx: Int, value: T, time: ITimeEx = TimeEx()) : this(NO_IDX, idx, value, time)
}