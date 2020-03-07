package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.*

sealed class BaseType {

    abstract class Event : BaseType() {
        open fun OnStateChanged.fired(event: Event) = Unit
        override fun toString(): String = this::class.name
    }

    abstract class State : BaseType() {
        open fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit
        override fun toString(): String = this::class.name
    }

    abstract class Data(var isSticky: Boolean = false) : BaseType()
}

open class ValueDataHolder(
    val value: BaseType,
    var data: Set<Data> = emptySet()
) {

    val hasState
        get() = value is State
    val hasEvent
        get() = value is Event

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : State> state() = value as T

    inline fun <reified T : Data> data(idx: Int = 0): T? = data.toList().filterIsInstance<T>().getOrNull(idx)

    override fun equals(other: Any?): Boolean =
        value == (other as? ValueDataHolder)?.value &&
                data == (other as? ValueDataHolder)?.data

    override fun hashCode(): Int = value.hashCode() + data.hashCode()
    override fun toString(): String = "${value::class.simpleName}(data=$data)"
}

val <T : BaseType> T.holder
    get() = ValueDataHolder(this)

val <T : BaseType> T?.toSet
    get() = this?.let { setOf(it) } ?: emptySet()