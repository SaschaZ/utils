package dev.zieger.utils.misc

abstract class AbsFiFo<out T>(
    initial: List<T> = emptyList(),
    protected val values: MutableList<@UnsafeVariance T> = ArrayList(initial)
) : List<T> by values {

    abstract val isFull: Boolean
    open val isNotFull: Boolean
        get() = !isFull

    open fun put(value: @UnsafeVariance T, update: Boolean = false): T? =
        if (update) onUpdate(value) else onInsert(value)

    protected open fun onUpdate(value: @UnsafeVariance T): T = value.also {
        if (isNotEmpty()) values[values.lastIndex] = it
        else values.add(it)
    }

    protected open fun onInsert(value: @UnsafeVariance T): T? {
        val removed = if (isFull) values.removeAt(0) else null
        values.add(value)
        return removed
    }

    open fun reset() = values.clear()
    fun takeLast(num: Int): List<T?> = values.takeLast(num)
}

open class FiFo<out T>(
    private val capacity: Int = 10,
    initial: List<T> = emptyList()
) : AbsFiFo<T>(initial) {

    override val isFull: Boolean
        get() = values.size == capacity
}