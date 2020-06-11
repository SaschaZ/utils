package dev.zieger.utils.misc

abstract class AbsFiFo<out T>(
    initial: List<T> = emptyList(),
    protected val values: MutableList<@UnsafeVariance T> = ArrayList(initial)
) : List<T> by values {

    abstract val isFull: Boolean
    open val isNotFull: Boolean
        get() = !isFull

    open fun put(value: @UnsafeVariance T, update: Boolean = false): List<T?> {
        if (update) onUpdate(value) else onInsert(value)
        return values
    }

    protected open fun onUpdate(value: @UnsafeVariance T) {
        if (isNotEmpty()) values[values.lastIndex] = value
        else values.add(value)
    }

    protected open fun onInsert(value: @UnsafeVariance T) {
        if (isFull) values.removeAt(0)
        values.add(value)
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