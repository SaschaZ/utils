package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.coroutines.channel.network.INodeValue.Companion.NO_IDX
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.IMillisecondHolder


interface INodeValue<out T> : IMillisecondHolder {

    companion object {

        const val NO_IDX = -1
    }

    val value: T
    val time: ITimeEx

    val inIdx: Int
    val outIdx: Int
    val idx: Int
        get() = outIdx

    operator fun component1(): T = value
    operator fun component2(): ITimeEx = time
}

data class NodeValue<T>(
    override val value: T,
    override val time: ITimeEx = TimeEx(),
    override val inIdx: Int = NO_IDX,
    override val outIdx: Int = NO_IDX
) : INodeValue<T>, IMillisecondHolder by time {

    constructor(
        inIdx: Int,
        outIdx: Int,
        value: T,
        time: ITimeEx = TimeEx()
    ) : this(value, time, inIdx, outIdx)
}