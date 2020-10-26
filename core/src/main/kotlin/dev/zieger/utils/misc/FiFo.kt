@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.misc

import java.util.*
import kotlin.collections.ArrayList

/**
 * Describes a FiFo (first in - first out) queue.
 */
interface IFiFo<T> : MutableList<T> {

    /**
     * `true` when next [put] call would remove the oldest item in the FiFo.
     */
    val isFull: Boolean

    /**
     * `true` when next put would not remove any items from the FiFo.
     */
    val isNotFull: Boolean get() = !isFull

    /**
     * Puts a new value into the FiFo queue.
     *
     * @param value Value to add to the FiFo queue.
     * @param update `true` when the latest value in the FiFo queue should be replaced with [value] instead of adding
     *   it as a new item to the queue. Defaulting to `false`.
     *
     * @return Items in the FiFo queue.
     */
    fun put(value: T, update: Boolean = false): List<T>

    /**
     * Removes the oldest item from the FiFo queue.
     *
     * @return The removed item or `null`.
     */
    fun take(): T?
}

abstract class BaseFiFo<T>(
    protected val initial: List<T> = emptyList(),
    protected val internal: MutableList<T> = LinkedList(initial),
    protected val doRemove: MutableList<T>.() -> Unit
) : IFiFo<T>, MutableList<T> by internal {

    override fun put(value: T, update: Boolean): List<T> {
        if (update && internal.isNotEmpty()) onUpdate(value) else onInsert(value)
        return internal
    }

    override fun take(): T? = internal.firstOrNull()?.also { internal.remove(it) }

    protected open fun onUpdate(value: T) {
        internal[internal.lastIndex] = value
    }

    protected open fun onInsert(value: T) {
        internal.add(value)
        internal.doRemove()
    }

    override fun toString(): String = "FiFo[${internal.joinToString(", ")}]"
}

/**
 * Implementation of [BaseFiFo] using a fixed [capacity] as FiFo size.
 */
open class FiFo<T>(
    val capacity: Int,
    initial: List<T> = emptyList()
) : BaseFiFo<T>(initial, doRemove = { while (size > capacity) removeAt(0) }) {

    /**
     * Copy constructor.
     */
    constructor(fifo: FiFo<T>) : this(fifo.capacity, ArrayList(fifo))

    override val isFull: Boolean get() = internal.size == capacity
}

