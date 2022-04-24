package dev.zieger.utils.time

import java.util.*

open class TimeStampFiFo<T : ITimeStamp> private constructor(
    val maxSpan: ITimeSpan,
    private val baseList: MutableList<T>
) : List<T> by baseList {

    constructor(maxSpan: ITimeSpan) : this(maxSpan, LinkedList())

    fun put(item: T) {
        if (TimeStamp() - item < maxSpan)
            baseList.add(item)
    }

    override fun get(index: Int): T = filter()[index]

    override val size: Int
        get() = filter().size

    override fun contains(element: T): Boolean = filter().contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = filter().containsAll(elements)

    override fun indexOf(element: T): Int = filter().indexOf(element)

    override fun isEmpty(): Boolean = filter().isEmpty()

    override fun iterator(): Iterator<T> = filter().iterator()

    override fun lastIndexOf(element: T): Int = filter().lastIndexOf(element)

    override fun listIterator(): ListIterator<T> = filter().listIterator()

    override fun listIterator(index: Int): ListIterator<T> = filter().listIterator(index)

    override fun spliterator(): Spliterator<T> = filter().spliterator()

    override fun subList(fromIndex: Int, toIndex: Int): List<T> = filter().subList(fromIndex, toIndex)

    private fun filter(): List<T> {
        val now = TimeStamp()
        return baseList.apply { removeAll { now - it > maxSpan } }
    }
}