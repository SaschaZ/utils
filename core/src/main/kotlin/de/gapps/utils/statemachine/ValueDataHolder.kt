package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.Event
import de.gapps.utils.statemachine.BaseType.State

sealed class BaseType {

    abstract class Event : BaseType() {
        override fun toString(): String = this::class.name
    }

    abstract class State : BaseType() {
        override fun toString(): String = this::class.name
    }
}

abstract class Data : BaseType()

open class ValueDataHolder(
    val value: BaseType,
    var data: Data? = null
) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : State> state() = value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : Data> data() = data as T

    override fun equals(other: Any?): Boolean =
        value == (other as? ValueDataHolder)?.value &&
                data == (other as? ValueDataHolder)?.data

    override fun hashCode(): Int = value.hashCode() + data.hashCode()
    override fun toString(): String = "${value::class.simpleName}(data=$data)"
}