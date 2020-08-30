package dev.zieger.utils.misc

import java.util.*

interface IFiFo<T> : List<T> {

    val isFull: Boolean
    val isNotFull: Boolean get() = !isFull

    fun put(value: T, update: Boolean = false): List<T>
    fun take(): T?

    fun clear()
}

abstract class BaseFiFo<T>(
    protected val initial: List<T> = emptyList(),
    protected val internal: MutableList<T> = LinkedList(initial),
    protected val shouldRemove: List<T>.(T) -> Boolean
) : IFiFo<T>, List<T> by internal {

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
        internal.removeAll { internal.shouldRemove(it) }
    }

    override fun clear() = internal.clear()
}

/**
 * Implementation of [BaseFiFo] using a fixed [capacity] as FiFo size.
 */
open class FiFo<T>(
    private val capacity: Int = 10,
    initial: List<T> = emptyList()
) : BaseFiFo<T>(initial, shouldRemove = { indexOf(it) >= capacity }) {

    override val isFull: Boolean get() = internal.size == capacity
}