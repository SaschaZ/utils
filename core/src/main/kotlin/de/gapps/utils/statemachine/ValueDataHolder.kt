@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.Event
import de.gapps.utils.statemachine.BaseType.State

/**
 * Base class for [Event]s and [State]s.
 */
sealed class BaseType {

    /**
     * All events need to implement this class.
     *
     * @property ignoreData  Set to `true` when the data of this event should have no influence when events get mapped.
     *                       Default is `false`.
     */
    abstract class Event(var ignoreData: Boolean = false) : BaseType() {

        /**
         * Is called when this event is applied to the state machine.
         */
        open fun OnStateChanged.fired() = Unit

        override fun toString(): String = this::class.name
    }

    /**
     * All states need to implement this class.
     */
    abstract class State : BaseType() {

        /**
         * Is called when this state gets activated an deactivated.
         *
         * @param isActive `true` when this states gets activated. `false` when it gets deactivated.
         */
        open fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

        override fun toString(): String = this::class.name
    }
}

/**
 * Every data needs to implement this class.
 *
 * @property isSticky Only relevant for data that is attached to states. When `true` the data of an active state is
 *                    attached to the next state that will get activated. Default is `false`.
 */
abstract class Data(var isSticky: Boolean = false) : BaseType()

/**
 * Container class for [Event]s or [State]s and their attached [Data].
 *
 * @property value [Event] or [State].
 * @property data [Set] of [Data] that is attached to the provided [BaseType].
 * @property exclude When `true` the provided value and data should not be part of a match.
 *                        Default is `false`.
 */
open class ValueDataHolder(
    val value: BaseType,
    var data: Set<Data> = emptySet(),
    var exclude: Boolean = false
) {

    /**
     * `true` when the provided [BaseType] is a [State].
     */
    val hasState
        get() = value is State

    /**
     * `true` when the provided [BaseType] is an [Event].
     */
    val hasEvent
        get() = value is Event

    /**
     * `true` when the [Data] of a provided [Event] should be ignored when matching.
     */
    val ignoreData = (value as? Event)?.ignoreData ?: false

    /**
     * Returns an event as the provided type [T]. (unsafe)
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = value as T

    /**
     * Returns a state as the provided type [T]. (unsafe)
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : State> state() = value as T

    /**
     * Returns [Data] of the provided type [T].
     *
     * @param idx Is used when there is more than one [Data] instance of the provided type [T].
     *            Default is `0`.
     */
    inline fun <reified T : Data> data(idx: Int = 0): T = data.toList().filterIsInstance<T>()[idx]

    override fun equals(other: Any?): Boolean =
        value == (other as? ValueDataHolder)?.value &&
                (ignoreData
                        || (other as? ValueDataHolder)?.ignoreData == true
                        || data == (other as? ValueDataHolder)?.data)

    override fun hashCode(): Int = value.hashCode() + if (ignoreData) 0 else data.hashCode()
    override fun toString(): String = "${value::class.name}(data=$data)"
}

/**
 * Creates a new [ValueDataHolder] instance with the [BaseType].
 */
val <T : BaseType> T.holder
    get() = ValueDataHolder(this)

/**
 * Creates a new [Set] with the [BaseType].
 */
val <T : BaseType> T?.toSet
    get() = this?.let { setOf(it) } ?: emptySet()