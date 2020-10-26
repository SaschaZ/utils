package dev.zieger.utils.misc

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx

open class LruCache<T>(
    private val isFull: List<Entry<T>>.() -> Boolean
) : List<T> {

    constructor(capacity: Int) : this({ size == capacity })

    companion object {

        data class Entry<T>(
            val value: T,
            var lastAccess: ITimeEx = TimeEx()
        )
    }

    private val internal = ArrayList<Entry<T>>()

    open fun put(value: T): T? {
        val removed = if (internal.isFull())
            internal.minBy { it.lastAccess }?.also { internal.remove(it) } else null
        internal.add(Entry(value))
        return removed?.value
    }

    override fun get(index: Int): T = internal[index].also { it.lastAccess = TimeEx() }.value

    override val size: Int get() = internal.size
    override fun contains(element: T): Boolean = internal.any { it.value == element }
    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }
    override fun indexOf(element: T): Int = internal.indexOfFirst { it.value == element }
    override fun isEmpty(): Boolean = internal.isEmpty()
    override fun iterator(): Iterator<T> = internal.map { it.value }.iterator()
    override fun lastIndexOf(element: T): Int = internal.indexOfLast { it.value == element }
    override fun listIterator(): ListIterator<T> = internal.map { it.value }.listIterator()
    override fun listIterator(index: Int): ListIterator<T> = internal.map { it.value }.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<T> = internal.map { it.value }.subList(fromIndex, toIndex)
}