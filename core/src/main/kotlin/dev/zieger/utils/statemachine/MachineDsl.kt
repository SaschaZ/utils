@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.conditionelements.*


abstract class MachineDsl : IMachineEx {

    protected val mapper: IMachineExMapper = MachineExMapper()

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
    operator fun Event.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun State.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun EventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun StateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
    operator fun PrevElement.times(slave: Slave): PrevElement = apply { combo.slave = slave }

    // Only the case when slave is added to first item. unary operators are processed before.
    operator fun Condition.times(slave: Slave) = apply {
        start.slave = slave
    }

    operator fun Condition.plus(other: PrevElement): Condition = apply { all += other }
    operator fun Condition.minus(other: PrevElement): Condition = apply { none += other }

    /**
     * Use the [Int] operator to match against one of the previous items. For example [State][3] will not try to match
     * against the current state, it will try to match against the third last [State] instead.
     */
    operator fun Master.get(idx: Int): PrevElement = this[idx..idx]
    operator fun Master.get(range: IntRange): PrevElement = PrevElement(combo, range)

    /**
     * Use the [not] operator to ignore any slaves for this element.
     */
    operator fun Event.not(): EventCombo = combo.apply { ignoreSlave = true }
    operator fun EventGroup<*>.not(): EventGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun State.not(): StateCombo = combo.apply { ignoreSlave = true }
    operator fun StateGroup<*>.not(): StateGroupCombo<*> = combo.apply { ignoreSlave = true }
    operator fun PrevElement.not(): PrevElement = apply { combo.ignoreSlave = true }

    /**
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = mapper.addBinding(this, machine)

    /**
     *
     */
    infix fun Condition.set(state: State): Unit = execAndSet { state }
    infix fun Condition.set(state: StateCombo): Unit = execAndSet { state }

    infix fun Condition.fire(event: Event): Unit = execAndFire { event }

    /**
     *
     */
    infix fun Condition.exec(block: suspend IMatchScope.() -> Unit) =
        mapper.addCondition(this) { block(); null }.asUnit()

    infix fun Condition.execAndSet(block: suspend IMatchScope.() -> State) =
        mapper.addCondition(this, block).asUnit()

    infix fun Condition.execAndFire(block: suspend IMatchScope.() -> Event) =
        mapper.addCondition(this) { fire eventSync block(); null }.asUnit()
}

operator fun Event.times(slave: Slave) = combo.also { it.slave = slave }
operator fun State.times(slave: Slave) = combo.also { it.slave = slave }
operator fun EventGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }
operator fun StateGroup<*>.times(slave: Slave) = combo.also { it.slave = slave }