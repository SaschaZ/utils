@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

val <D : Any> IMachineEx<D>.on get() = OnScope(this)

class OnScope<out D : Any>(machine: IMachineEx<D>) :
    IMachineEx<D> by machine {

    infix fun event(event: IEvent<@UnsafeVariance D>) = event(setOf(event))
    infix fun event(events: Set<IEvent<@UnsafeVariance D>>) =
        OnEventScope(this, events)

    class OnEventScope<out D : Any>(
        onScope: OnScope<D>,
        events: Set<IEvent<D>>
    ) : FullExecutor<D>(onScope, events) {

        infix fun andState(state: IState<@UnsafeVariance D>) = FullExecutor(this, events, setOf(state))
        infix fun andState(states: Set<IState<@UnsafeVariance D>>) = FullExecutor(this, events, states)
    }

    infix fun <D : Any> state(states: IState<D>) = stateEnter(states)
    infix fun <D : Any> state(states: Set<IState<D>>) = stateEnter(states)
    infix fun <D : Any> stateEnter(states: IState<D>) = Executor(this, emptySet(), setOf(states), true)
    infix fun <D : Any> stateEnter(states: Set<IState<D>>) = Executor(this, emptySet(), states, true)
    infix fun <D : Any> stateExit(states: IState<D>) = Executor(this, emptySet(), setOf(states), false)
    infix fun <D : Any> stateExit(states: Set<IState<D>>) = Executor(this, emptySet(), states, false)
}