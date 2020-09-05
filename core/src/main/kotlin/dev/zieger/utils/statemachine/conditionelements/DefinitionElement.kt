@file:Suppress("unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MatchScope
import dev.zieger.utils.statemachine.OnStateChanged
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*
import kotlin.math.max

sealed class DefinitionElement : ConditionElement() {

    open val hasExternal: Boolean = false
    open val hasEvent: Boolean = false
    open val hasState: Boolean = false
    open val hasStateGroup: Boolean = false
    open val hasEventGroup: Boolean = false
    open val hasPrevElement: Boolean = false

    @Suppress("LeakingThis")
    val hasGroup: Boolean = hasStateGroup || hasEventGroup

    val type: DefinitionType
        get() = when {
            hasState || hasStateGroup -> STATE
            hasEvent || hasEventGroup -> EVENT
            hasExternal -> EXTERNAL
            else -> throw IllegalArgumentException("Can not build DefinitionType for $this")
        }
}

/**
 *
 */
@Suppress("EmptyRange", "PropertyName")
val X: IntRange = 0..-1

data class PrevElement(
    val combo: ComboElement,
    val range: IntRange
) : DefinitionElement() {

    override val hasEvent = combo.master is Event
    override val hasState = combo.master is State
    override val hasStateGroup = combo.master is StateGroup<*>
    override val hasEventGroup = combo.master is EventGroup<*>
    override val hasPrevElement = true

    val idx: Int = max(range.first, range.last)

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean =
        combo.run { matchPrev(other, previousChanges, range, idx) { match(it) } }

    private inline fun ConditionElement?.matchPrev(
        other: ConditionElement?,
        previousChanges: List<OnStateChanged>,
        range: IntRange,
        idx: Int,
        block: ConditionElement?.(ConditionElement?) -> Boolean
    ): Boolean = when {
        this == null -> false
        range.first == range.last -> block(valueForIndex(idx, other, previousChanges))
        range.first > range.last -> (0..previousChanges.lastIndex).any {
            block(valueForIndex(it, other, previousChanges))
        }
        else -> range.any { block(valueForIndex(it, other, previousChanges)) }
    }

    private fun valueForIndex(
        idx: Int,
        other: ConditionElement?,
        previousChanges: List<OnStateChanged>
    ): ConditionElement? = when {
        idx == 0 -> other
        idx < 0 || idx > previousChanges.lastIndex -> null
        hasEvent -> if (idx > 0) previousChanges[idx - 1].event else other
        hasState -> previousChanges[idx].stateAfter
        else -> throw IllegalArgumentException("Can not access previous value for $other")
    }

    override fun toString(): String = "PE(${combo.toString().removeSuffix(")")}|$range|$idx)"
}

open class ComboStateElement(
    master: State,
    slave: Slave? = null,
    usedAs: UsedAs = UsedAs.DEFINITION,
    ignoreSlave: Boolean = false
) : ComboBaseElement<State, Slave>(master, slave, usedAs, ignoreSlave), IResultState

open class ComboEventElement(
    master: Event,
    slave: Slave? = null,
    usedAs: UsedAs = UsedAs.DEFINITION,
    ignoreSlave: Boolean = false
) : ComboBaseElement<Event, Slave>(master, slave, usedAs, ignoreSlave), IResultEvent

typealias ComboElement = ComboBaseElement<Master, Slave>
typealias ComboStateGroupElement = ComboBaseElement<StateGroup<*>, Slave>
typealias ComboEventGroupElement = ComboBaseElement<EventGroup<*>, Slave>

open class ComboBaseElement<out M : Master, S : Slave>(
    val master: M,
    var slave: S? = null,
    var usedAs: UsedAs = UsedAs.DEFINITION,
    var ignoreSlave: Boolean = false
) : DefinitionElement() {

    override val hasEvent = master is Event
    override val hasState = master is State
    override val hasStateGroup = master is StateGroup<*>
    override val hasEventGroup = master is EventGroup<*>

    override suspend fun MatchScope.match(
        other: ConditionElement?
    ): Boolean {
        return when (other) {
            is ComboBaseElement<*, *> ->
                master.run { match(other.master) }
                        && (slave == null && other.slave == null
                        || ignoreSlave || other.ignoreSlave
                        || slave?.run { match(other.slave) } == true)
            is InputElement -> other.run { match(this@ComboBaseElement) }
            null -> false
            else -> throw IllegalArgumentException(
                "Can not match ${this@ComboBaseElement::class.name} " +
                        "with ${other.let { it::class.name }}"
            )
        } logV {
            f = LogFilter.Companion.GENERIC(
                disableLog = noLogging || other.noLogging
                        || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO
            )
            m = "#CE $it => ${this@ComboBaseElement} <||> $other"
        }
    }

    override fun toString() = "CE($master|$slave|$ignoreSlave|${
        when (master) {
            is Event -> "E"
            is State -> "S"
            is EventGroup<*> -> "Eg"
            is StateGroup<*> -> "Sg"
            is External -> "X"
            else -> "?[${master::class}]"
        }
    }${usedAs.name[0]})"

    override fun equals(other: Any?): Boolean = (other as? ComboBaseElement<*, *>)?.let { o ->
        master == o.master &&
                slave == o.slave &&
                ignoreSlave == o.ignoreSlave
    } ?: false

    override fun hashCode(): Int =
        master.hashCode() + slave.hashCode() + if (ignoreSlave) 1 else 0
}

val ComboElement.isDefinition get() = usedAs == UsedAs.DEFINITION
val ComboElement.isRuntime get() = usedAs == UsedAs.RUNTIME

infix fun <M : Event, S : Slave> M.comboEvent(slave: S?) = ComboEventElement(this, slave)
infix fun <M : EventGroup<*>, S : Slave> M.comboEventGroup(slave: S?) = ComboEventGroupElement(this, slave)
infix fun <M : State, S : Slave> M.comboState(slave: S?) = ComboStateElement(this, slave)
infix fun <M : StateGroup<*>, S : Slave> M.comboStateGroup(slave: S?) = ComboStateGroupElement(this, slave)

val <E : Event> E.comboEvent get() = ComboEventElement(this)
val <E : EventGroup<*>> E.comboEventGroup get() = ComboEventGroupElement(this)
val <S : State> S.comboState get() = ComboStateElement(this)
val <S : StateGroup<*>> S.comboStateGroup get() = ComboStateGroupElement(this)

/**
 * External condition.
 * Is checked at runtime. All External's need to match within a condition.
 */
open class External(val condition: suspend MatchScope.() -> Boolean) : DefinitionElement() {

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean =
        condition() logV {
            f = LogFilter.Companion.GENERIC(
                disableLog = noLogging || other.noLogging
                        || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO
            )
            m = "#EX $it => ${this@External} <||> $other"
        }

    override val hasExternal = true

    override fun toString(): String = "External"
}