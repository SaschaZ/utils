package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

open class Executor<out D : IData, out E : IEvent<D>, out S : IState>(
    machine: IMachineEx<D, E, S>,
    val events: Set<E> = emptySet(),
    val states: Set<S> = emptySet(),
    val isStateEnter: Boolean = false
) : IMachineEx<D, E, S> by machine {

    infix fun exec(block: ExecutorScope<D, E, S>.() -> Unit) =
        mapper.addMapping(states) { event, state -> ExecutorScope(event, state).block() }
}

open class FullExecutor<out D : IData, out E : IEvent<D>, out S : IState>(
    machine: IMachineEx<D, E, S>,
    events: Set<E> = emptySet(),
    states: Set<S> = emptySet()
) : Executor<D, E, S>(machine, events, states) {

    infix fun execAndSet(block: ExecutorScope<D, E, S>.() -> @UnsafeVariance S) =
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }

    infix fun set(state: @UnsafeVariance S) = mapper.addMapping(events, states) { _, _ -> state }
}

data class ExecutorScope<out D : IData, out E : IEvent<D>, out S : IState>(
    val event: E,
    val state: S
) {
    val data: D? = event.data
}