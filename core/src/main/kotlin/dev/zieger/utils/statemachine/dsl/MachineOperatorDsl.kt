@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineOperatorDsl : MachineDslRoot {

    // start entry with unary +
    operator fun AbsEventType.unaryPlus() = EventCondition(this)
    operator fun AbsStateType.unaryPlus() = StateCondition(this)

    // link wanted items with + operator
    operator fun <C : Condition> C.plus(other: Master): C = apply { any += other }
    operator fun <C : Condition> C.plus(other: suspend IMatchScope.() -> Boolean): C = apply { all += External(other) }
    operator fun <C : Condition> C.plus(other: Previous<*>): C = apply { all += other }

    // link unwanted items with - operator
    operator fun <C : Condition> C.minus(other: Master): C = apply { none += other }
    operator fun <C : Condition> C.minus(other: suspend IMatchScope.() -> Boolean): C =
        apply { none += External(other) }

    // apply Data with * operator
    operator fun AbsEvent.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsState.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsEventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsStateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun StatePrevious.times(slave: Slave) = apply { combo.slave = slave }
    operator fun EventPrevious.times(slave: Slave) = apply { combo.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun <C : Condition> C.times(slave: Slave): C = apply {
        start.slave = slave
    }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [AbsState] instead.
     */
    operator fun AbsState.get(idx: Int) = this[idx..idx]
    operator fun AbsState.get(range: IntRange) = StatePrevious(combo, range)
    operator fun AbsEvent.get(idx: Int) = this[idx..idx]
    operator fun AbsEvent.get(range: IntRange) = EventPrevious(combo, range)

    /**
     * Use the [not] operator to ignore any slaves for this element.
     */
    operator fun AbsEvent.not(): EventCombo = combo.apply { ignoreSlave = true }
    operator fun AbsEventGroup<*>.not(): EventGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun AbsState.not(): StateCombo = combo.apply { ignoreSlave = true }
    operator fun AbsStateGroup<*>.not(): StateGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun EventPrevious.not() = apply { combo.ignoreSlave = true }
    operator fun StatePrevious.not() = apply { combo.ignoreSlave = true }
}

operator fun AbsEvent.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsState.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsEventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsStateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }