package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MatchScope
import dev.zieger.utils.statemachine.OnStateChanged
import kotlin.reflect.KClass

/**
 * Base class for [Event]s and [State]s.
 */
sealed class Master : ConditionElement() {

    internal fun condition() =  when (this) {
        is Event -> Condition(this)
        is EventGroup<*> -> Condition(this)
        is State -> Condition(this)
        is StateGroup<*> -> Condition(this)
    }

    val combo: ComboBaseElement<*, Slave> get() = when (this) {
        is Event -> this.comboEvent
        is State -> this.comboState
        is EventGroup<*> -> this.comboEventGroup
        is StateGroup<*> -> this.comboStateGroup
    }
}

sealed class Group<T : Single>(val type: KClass<T>) : Master()

sealed class Single : Master()

/**,
 * All events need to implement this class.
 * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
 */
abstract class Event(
    open val noLogging: Boolean = false
) : Single(), IResultState {

    fun OnStateChanged.fired() = Unit

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is Event -> this@Event === other
            is EventGroup<*> -> other.run { match(this@Event) }
            else -> false
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#E $it => ${this@Event} <||> $other"
        }
    }}

/**
 * All states need to implement this class.
 */
abstract class State : Single(), IResultState {

    fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is State -> this@State === other
            is StateGroup<*> -> other.run { match(this@State) }
            else -> false
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#ST $it => ${this@State} <||> $other"
        }
    }
}

abstract class EventGroup<T : Event>(type: KClass<T>) : Group<T>(type) {

    override suspend fun MatchScope.match(
        other: ConditionElement?
    ): Boolean {
        return when (other) {
            is Event -> type.isInstance(other)
            is EventGroup<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@EventGroup::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#EG $it => ${this@EventGroup} <||> $other"
        }
    }

    override fun toString(): String = type.name
}

abstract class StateGroup<T : State>(type: KClass<T>) : Group<T>(type) {

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is State -> type.isInstance(other)
            is StateGroup<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@StateGroup::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#SG $it => ${this@StateGroup} <||> $other"
        }
    }

    override fun toString(): String = type.name
}