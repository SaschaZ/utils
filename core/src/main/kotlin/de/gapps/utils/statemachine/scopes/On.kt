@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

val IMachineEx<*, *, *>.on get() = OnScope(this)

class OnScope<out D : IData, out E : IEvent<D>, out S : IState>(machine: IMachineEx<D, E, S>) :
    IMachineEx<D, E, S> by machine {

    infix fun event(event: @UnsafeVariance E) = event(setOf(event))
    infix fun event(events: Set<@UnsafeVariance E>) =
        OnEventScope(this, events)

    class OnEventScope<out D : IData, out E : IEvent<D>, out S : IState>(
        onScope: OnScope<D, E, S>,
        events: Set<E>
    ) : FullExecutor<D, E, S>(onScope, events) {

        infix fun andState(state: IState) = FullExecutor(this, events, setOf(state))
        infix fun andState(states: Set<IState>) = FullExecutor(this, events, states)
    }

    infix fun state(states: @UnsafeVariance S) = stateEnter(states)
    infix fun state(states: Set<@UnsafeVariance S>) = stateEnter(states)
    infix fun stateEnter(states: @UnsafeVariance S) = Executor(this, emptySet(), setOf(states), true)
    infix fun stateEnter(states: Set<@UnsafeVariance S>) = Executor(this, emptySet(), states, true)
    infix fun stateExit(states: @UnsafeVariance S) = Executor(this, emptySet(), setOf(states), false)
    infix fun stateExit(states: Set<@UnsafeVariance S>) = Executor(this, emptySet(), states, false)
}