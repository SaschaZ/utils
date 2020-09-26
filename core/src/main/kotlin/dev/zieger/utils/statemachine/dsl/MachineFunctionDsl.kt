@file:Suppress("unused")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.statemachine.IMachineEx
import dev.zieger.utils.statemachine.IMachineExMapper
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineFunctionDsl : IMachineEx {

    val mapper: IMachineExMapper

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

    fun EventCondition.withState(vararg states: AbsState) = apply { any.addAll(states) }
    fun EventCondition.withoutState(vararg states: AbsState) = apply { none.addAll(states) }

    fun StateCondition.withEvent(vararg events: AbsEvent) = apply { any.addAll(events) }
    fun StateCondition.withoutEvent(vararg events: AbsEvent) = apply { none.addAll(events) }

    fun Condition.withPrevious(vararg states: PrevElement) = apply { all.addAll(states) }
    fun Condition.withoutPrevious(vararg states: PrevElement) = apply { none.addAll(states) }
}

operator fun Event.invoke(slave: Slave) = EventCombo(this, slave)
operator fun State.invoke(slave: Slave) = StateCombo(this, slave)
operator fun EventGroup<*>.invoke(slave: Slave) = EventGroupCombo(this, slave)
operator fun StateGroup<*>.invoke(slave: Slave) = StateGroupCombo(this, slave)