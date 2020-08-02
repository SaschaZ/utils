package dev.zieger.utils.misc

import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx
import dev.zieger.utils.time.minus

abstract class AbsFiFo<T>(
    initial: List<T> = emptyList(),
    protected val values: MutableList<T> = ArrayList(initial),
    protected val toRemove: List<T>.(T) -> Boolean
) : List<T> by values {

    abstract val isFull: Boolean
    open val isNotFull: Boolean get() = !isFull

    open fun put(value: T, update: Boolean = false): List<T>? =
        if (update) onUpdate(value) else onInsert(value)

    protected open fun onUpdate(value: T): List<T> = listOf(value.also {
        if (isNotEmpty()) values[values.lastIndex] = it
        else values.add(it)
    })

    protected open fun onInsert(value: T): List<T>? = values.filter { values.run { toRemove(it) } }.also {
        values += value
    }

    open fun reset() = values.clear()
    fun takeLast(num: Int): List<T?> = values.takeLast(num)
}

open class FiFo<T>(
    private val capacity: Int = 10,
    initial: List<T> = emptyList()
) : AbsFiFo<T>(initial, toRemove = { indexOf(it) >= capacity }) {

    override val isFull: Boolean get() = values.size == capacity
}

open class DurationFiFo<T : ITimeEx>(
    private val maxDuration: IDurationEx,
    initial: List<T> = emptyList()
) : AbsFiFo<T>(initial, toRemove = {  min()?.let { f -> (it - f) > maxDuration } ?: false }) {
    override val isFull: Boolean
        get() = whenNotNull(values.firstOrNull(), values.lastOrNull()) { f, l ->
            (l - f) > maxDuration
        } ?: false
}