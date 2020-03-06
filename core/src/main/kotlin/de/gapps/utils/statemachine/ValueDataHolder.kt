package de.gapps.utils.statemachine

import de.gapps.utils.misc.name

abstract class Event {
    override fun toString(): String = this::class.name
}
abstract class State {
    override fun toString(): String = this::class.name
}

open class ValueDataHolder<V: Any>(
    val value: V,
    var data: Any? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : State> state() = value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> data() = data as T

    override fun equals(other: Any?): Boolean = value == (other as? ValueDataHolder<*>)?.value &&
            data == (other as? ValueDataHolder<*>)?.data

    override fun hashCode(): Int = value.hashCode() + data.hashCode()
    override fun toString(): String = "${value::class.simpleName}(data=$data)"
}