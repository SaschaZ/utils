@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.IMachineEx
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineOperatorDsl : MachineDslRoot {

    // start entry with unary +
    operator fun Master.unaryPlus() = Condition(this)

    // link wanted items with + operator
    operator fun Condition.plus(other: Master): Condition = apply { any += other }
    operator fun Condition.plus(other: suspend IMatchScope.() -> Boolean): Condition = apply { all += External(other) }

    // link unwanted items with - operator
    operator fun Condition.minus(other: Master): Condition = apply { none += other }
    operator fun Condition.minus(other: suspend IMatchScope.() -> Boolean): Condition =
        apply { none += External(other) }

    // apply Data with * operator
    operator fun AbsEvent.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsState.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsEventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun AbsStateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun PrevElement.times(slave: Slave): PrevElement = apply { combo.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: Slave) = apply {
        start.slave = slave
    }

    operator fun Condition.plus(other: PrevElement): Condition = apply { all += other }
    operator fun Condition.minus(other: PrevElement): Condition = apply { none += other }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [AbsState] instead.
     */
    operator fun Master.get(idx: Int): PrevElement = this[idx..idx]
    operator fun Master.get(range: IntRange): PrevElement = PrevElement(combo, range)

    /**
     * Use the [not] operator to ignore any slaves for this element.
     */
    operator fun AbsEvent.not(): EventCombo = combo.apply { ignoreSlave = true }
    operator fun AbsEventGroup<*>.not(): EventGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun AbsState.not(): StateCombo = combo.apply { ignoreSlave = true }
    operator fun AbsStateGroup<*>.not(): StateGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun PrevElement.not(): PrevElement = apply { combo.ignoreSlave = true }

    /**
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = mapper.addBinding(this, machine)

    /**
     *
     */
    infix fun Condition.set(state: AbsState): Unit = execAndSet { state }
    infix fun Condition.set(state: StateCombo): Unit = execAndSet { state }

    infix fun Condition.fire(event: AbsEvent): Unit = execAndFire { event }

    /**
     *
     */
    infix fun Condition.exec(block: suspend IMatchScope.() -> Unit) =
        mapper.addCondition(this) { block(); null }.asUnit()

    infix fun Condition.execAndSet(block: suspend IMatchScope.() -> AbsState) =
        mapper.addCondition(this, block).asUnit()

    infix fun Condition.execAndFire(block: suspend IMatchScope.() -> AbsEvent) =
        mapper.addCondition(this, block).asUnit()
}

operator fun AbsEvent.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsState.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsEventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
operator fun AbsStateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }