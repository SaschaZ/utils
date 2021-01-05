@file:Suppress("unused")

package dev.zieger.utils.statemachine.dsl

import dev.zieger.utils.statemachine.IMachineEx
import dev.zieger.utils.statemachine.conditionelements.*

interface MachineFunctionDsl : MachineDslRoot {

    fun rawCondition(all: List<Definition>, any: List<Definition>, none: List<Definition>) =
        RawCondition(
            listOf(
                DefinitionGroup(DefinitionGroup.MatchType.ALL, ArrayList(all)),
                DefinitionGroup(DefinitionGroup.MatchType.ANY, ArrayList(any)),
                DefinitionGroup(DefinitionGroup.MatchType.NONE, ArrayList(none))
            )
        )

    fun onEvent(event: AbsEventType) = EventCondition(event)
    fun onState(state: AbsStateType) = StateCondition(state)

    operator fun Event.invoke(slave: Slave) = EventCombo(this, slave)
    operator fun State.invoke(slave: Slave) = StateCombo(this, slave)
    operator fun EventGroup<*>.invoke(slave: Slave) = EventGroupCombo(this, slave)
    operator fun StateGroup<*>.invoke(slave: Slave) = StateGroupCombo(this, slave)

    val Event.ignoreSlave get() = EventCombo(this, matchMasterOnly = true)
    val State.ignoreSlave get() = StateCombo(this, matchMasterOnly = true)
    val EventGroup<*>.ignoreSlave get() = EventGroupCombo(this, matchMasterOnly = true)
    val StateGroup<*>.ignoreSlave get() = StateGroupCombo(this, matchMasterOnly = true)
    val EventPrevious.ignoreSlave get() = apply { combo.matchMasterOnly = true }
    val StatePrevious.ignoreSlave get() = apply { combo.matchMasterOnly = true }

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