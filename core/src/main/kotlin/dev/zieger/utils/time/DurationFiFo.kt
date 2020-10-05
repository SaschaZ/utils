@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.time

import dev.zieger.utils.misc.BaseFiFo
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx

/**
 * FiFo queue for [ITimeEx] values limited by the duration from the oldest to the latest item.
 * The value that is put on the FiFo needs to be newer than the latest value in the FiFo.
 *
 * @property maxDuration Items that have a larger duration than [maxDuration] to the latest item will be removed.
 * @param initial Initial items for the FiFo queue.
 */
open class DurationFiFo<T : ITimeEx>(
    val maxDuration: IDurationEx,
    initial: List<T> = emptyList()
) : BaseFiFo<T>(initial, doRemove = { while (first().time < last().time - maxDuration) removeAt(0) }) {

    /**
     * Copy constructor.
     */
    constructor(fifo: DurationFiFo<T>) : this(fifo.maxDuration, ArrayList(fifo))

    override val isFull: Boolean
        get() = internal.run { isNotEmpty() && max()!! - min()!! > maxDuration }

    override fun put(value: T, update: Boolean): List<T> {
        require(value.time > internal.last().time) { "Value to put is not newer than latest value." }
        return super.put(value, update)
    }
}