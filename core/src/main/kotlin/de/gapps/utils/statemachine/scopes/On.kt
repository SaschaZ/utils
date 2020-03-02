package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

val IMachineEx<*, *, *>.on get() = OnScope(this)

class OnScope<out D: IData, out E: IEvent, out S: IState>(machine: IMachineEx<D, E, S>) : IMachineEx<D, E, S> by machine {

    infix fun event(event: IEvent) = event(setOf(event))
    infix fun event(events: Set<IEvent>) =
        OnEventScope(this, events)

    data class OnEventScope<out D : IData, out E : IEvent, out S : IState>(
        val onScope: OnScope<D, E, S>,
        val events: Set<E>
    ) : Executors<D, E, S>(onScope, events) {

        infix fun withState(states: IState) = OnEventStateScope(this, setOf(states))
        infix fun withState(states: Set<IState>) = OnEventStateScope(this, states)

        data class OnEventStateScope<out D : IData, out E : IEvent, out S : IState>(
            val onEventScope: OnEventScope<D, E, S>,
            val states: Set<S>
        ) : Executors<D, E, S>(onEventScope, onEventScope.events, states)
    }

    infix fun state(states: IState) = stateEnter(states)
    infix fun state(states: Set<IState>) = stateEnter(states)
    infix fun stateEnter(states: IState) = OnStateChangedScope(this, setOf(states), true)
    infix fun stateEnter(states: Set<IState>) = OnStateChangedScope(this, states, true)
    infix fun stateExit(states: IState) = OnStateChangedScope(this, setOf(states), false)
    infix fun stateExit(states: Set<IState>) = OnStateChangedScope(this, states, false)

    data class OnStateChangedScope<out D : IData, out E : IEvent, out S : IState>(
        val onScope: OnScope<D, E, S>,
        val states: Set<S>,
        val isStateEnter: Boolean
    ) {

        infix fun exec(block: () -> Unit) = onScope.mapper.addMapping(states) { block() }
    }
}

open class Executors<out D: IData, out E: IEvent, out S: IState>(
    private val machine: IMachineEx<D, E, S>,
    private val events: Set<E> = emptySet(),
    private val states: Set<S> = emptySet()
) : IMachineEx<D, E, S> by machine {

    infix fun exec(block: () -> Unit) = mapper.addMapping(events, states) { block(); null }
    infix fun execAndSet(block: () -> @UnsafeVariance S) = mapper.addMapping(events, states, block)
    infix fun set(state: @UnsafeVariance S) = mapper.addMapping(events, states) { state }
}

