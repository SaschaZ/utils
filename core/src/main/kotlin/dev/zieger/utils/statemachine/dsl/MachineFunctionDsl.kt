@file:Suppress("unused")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.statemachine.IMachineEx
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineFunctionDsl : MachineDslRoot {

    fun onEvent(vararg events: AbsEventType) = EventCondition(*events)
    fun onState(vararg states: AbsStateType) = StateCondition(*states)

    operator fun Event.invoke(slave: Slave) = EventCombo(this, slave)
    operator fun State.invoke(slave: Slave) = StateCombo(this, slave)
    operator fun EventGroup<*>.invoke(slave: Slave) = EventGroupCombo(this, slave)
    operator fun StateGroup<*>.invoke(slave: Slave) = StateGroupCombo(this, slave)

    val Event.ignoreSlave get() = EventCombo(this, ignoreSlave = true)
    val State.ignoreSlave get() = StateCombo(this, ignoreSlave = true)
    val EventGroup<*>.ignoreSlave get() = EventGroupCombo(this, ignoreSlave = true)
    val StateGroup<*>.ignoreSlave get() = StateGroupCombo(this, ignoreSlave = true)
    val EventPrevious.foo get() = apply { combo.ignoreSlave = true }
    val StatePrevious.foo get() = apply { combo.ignoreSlave = true }

    fun EventCondition.withState(vararg states: AbsStateType) = apply {
        states.forEach {
            when (it) {
                is Previous<*> -> all.add(it)
                else -> any.add(it)
            }
        }
    }

    fun EventCondition.withoutState(vararg states: AbsStateType) = apply { none.addAll(states) }

    fun StateCondition.withEvent(vararg events: AbsEventType) = apply {
        events.forEach {
            when (it) {
                is Previous<*> -> all.add(it)
                else -> any.add(it)
            }
        }
    }

    fun StateCondition.withoutEvent(vararg events: AbsEventType) = apply { none.addAll(events) }

    /**
     * Binds the [Condition] to the specified [IMachineEx].
     * A bound [IMachineEx] will process all events as long as the condition matches.
     */
    infix fun Condition.bind(machine: IMachineEx) = procesor.addBinding(this, machine)
}

operator fun Event.invoke(slave: Slave) = EventCombo(this, slave)
operator fun State.invoke(slave: Slave) = StateCombo(this, slave)
operator fun EventGroup<*>.invoke(slave: Slave) = EventGroupCombo(this, slave)
operator fun StateGroup<*>.invoke(slave: Slave) = StateGroupCombo(this, slave)