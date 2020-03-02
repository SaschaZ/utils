@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

val IMachineEx.on get() = OnScope(this)

class OnScope(machine: IMachineEx) :
    IMachineEx by machine {

    infix fun event(event: IEvent) = event(setOf(event))
    infix fun event(events: Set<IEvent>) =
        OnEventScope(this, events)

    class OnEventScope(
        onScope: OnScope,
        events: Set<IEvent>
    ) : FullExecutor(onScope, events) {

        infix fun andState(state: IState) = FullExecutor(this, events, setOf(state))
        infix fun andState(states: Set<IState>) = FullExecutor(this, events, states)
    }

    infix fun state(states: IState) = stateEnter(states)
    infix fun state(states: Set<IState>) = stateEnter(states)
    infix fun stateEnter(states: IState) = Executor(this, emptySet(), setOf(states), true)
    infix fun stateEnter(states: Set<IState>) = Executor(this, emptySet(), states, true)
    infix fun stateExit(states: IState) = Executor(this, emptySet(), setOf(states), false)
    infix fun stateExit(states: Set<IState>) = Executor(this, emptySet(), states, false)
}