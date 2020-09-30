package dev.zieger.utils.misc

open class DelegateList<out T>(
    private val delegate: () -> List<T>
) : List<T> {
    override val size: Int get() = delegate().size

    override fun contains(element: @UnsafeVariance T): Boolean = delegate().contains(element)

    override fun containsAll(elements: Collection<@UnsafeVariance T>): Boolean = delegate().containsAll(elements)

    override fun get(index: Int): T = delegate()[index]

    override fun indexOf(element: @UnsafeVariance T): Int = delegate().indexOf(element)

    override fun isEmpty(): Boolean = delegate().isEmpty()

    override fun iterator(): Iterator<T> = delegate().iterator()

    override fun lastIndexOf(element: @UnsafeVariance T): Int = delegate().lastIndexOf(element)

    override fun listIterator(): ListIterator<T> = delegate().listIterator()

    override fun listIterator(index: Int): ListIterator<T> = delegate().listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> = delegate().subList(fromIndex, toIndex)
}

abstract class AbsFiFo<T>(
    initial: List<T> = emptyList(),
    protected val values: MutableList<T> = ArrayList(initial)
) : List<T> by values {

    abstract val isFull: Boolean
    open val isNotFull: Boolean get() = !isFull

    open fun put(value: T, update: Boolean = false): T? =
        if (update) onUpdate(value) else onInsert(value)

    protected open fun onUpdate(value: T): T = value.also {
        if (isNotEmpty()) values[values.lastIndex] = it
        else values.add(it)
    }

    protected open fun onInsert(value: T): T? = values.add(value).let { if (isFull) values.removeAt(0) else null }

    open fun reset() = values.clear()
    fun takeLast(num: Int): List<T?> = values.takeLast(num)
}

open class FiFo<T>(
    private val capacity: Int = 10,
    initial: List<T> = emptyList()
) : AbsFiFo<T>(initial) {

    override val isFull: Boolean get() = values.size >= capacity
}