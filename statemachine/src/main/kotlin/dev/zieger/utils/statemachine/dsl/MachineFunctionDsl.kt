@file:Suppress("unused")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.statemachine.IMachineEx
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineFunctionDsl : MachineDslRoot {

    fun onEvent(event: AbsEventType) = EventCondition(event)
    fun onState(state: AbsStateType) = StateCondition(state)

    infix fun Event.data(slave: Slave) = EventCombo(this, slave)
    infix fun State.data(slave: Slave) = StateCombo(this, slave)
    infix fun EventGroup<*>.data(slave: Slave) = EventGroupCombo(this, slave)
    infix fun StateGroup<*>.data(slave: Slave) = StateGroupCombo(this, slave)
    infix fun <T : Master, P : Previous<T>> P.data(slave: Slave): P = apply { combo.slave = slave }

    val Event.ignoreSlave get() = EventCombo(this, matchMasterOnly = true)
    val State.ignoreSlave get() = StateCombo(this, matchMasterOnly = true)
    val EventGroup<*>.ignoreSlave get() = EventGroupCombo(this, matchMasterOnly = true)
    val StateGroup<*>.ignoreSlave get() = StateGroupCombo(this, matchMasterOnly = true)
    val EventPrevious.ignoreSlave get() = apply { combo.matchMasterOnly = true }
    val StatePrevious.ignoreSlave get() = apply { combo.matchMasterOnly = true }

    infix fun AbsState.previous(idx: Int) = previous(idx..idx)
    infix fun AbsState.previous(range: IntRange) = StatePrevious(combo, range)
    infix fun AbsEvent.previous(idx: Int) = previous(idx..idx)
    infix fun AbsEvent.previous(range: IntRange) = EventPrevious(combo, range)

    fun EventCondition.andState(vararg states: AbsStateType) = apply {
        states.forEach {
            when (it) {
                is Previous<*> -> all.add(it)
                else -> any.add(it)
            }
        }
    }

    fun EventCondition.withoutState(vararg states: AbsStateType) = apply { none.addAll(states) }

    fun StateCondition.andEvent(vararg events: AbsEventType) = apply {
        events.forEach {
            when (it) {
                is Previous<*> -> all.add(it)
                else -> any.add(it)
            }
        }
    }

    fun StateCondition.withoutEvent(vararg events: AbsEventType) = apply { none.addAll(events) }

    fun <C : Condition> C.and(block: suspend IMatchScope.() -> Boolean): C = apply { all.add(External(block)) }

    /**
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = processor.addBinding(this, machine)


    fun <C : Condition> C.all(vararg items: Master): C = apply { all.addAll(items) }
    fun <C : Condition> C.any(vararg items: Master): C = apply { any.addAll(items) }
    fun <C : Condition> C.none(vararg items: Master): C = apply { none.addAll(items) }
}

infix fun Event.data(slave: Slave) = EventCombo(this, slave)
infix fun State.data(slave: Slave) = StateCombo(this, slave)
infix fun EventGroup<*>.data(slave: Slave) = EventGroupCombo(this, slave)
infix fun StateGroup<*>.data(slave: Slave) = StateGroupCombo(this, slave)
infix fun <T : Master, P : Previous<T>> P.data(testStateData: Slave): P = apply { combo.slave = testStateData }