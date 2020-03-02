package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.*

open class Executor<out D : IData, out E : IEvent<D>, out S : IState>(
    machine: IMachineEx<D, E, S>,
    val events: Set<E> = emptySet(),
    val states: Set<S> = emptySet(),
    val isStateEnter: Boolean = false
) : IMachineEx<D, E, S> by machine {

    infix fun exec(block: (event: @UnsafeVariance E, state: @UnsafeVariance S) -> Unit) =
        mapper.addMapping(states) { p1, p2 -> block(p1, p2) }
}

open class FullExecutor<out D : IData, out E : IEvent<D>, out S : IState>(
    machine: IMachineEx<D, E, S>,
    events: Set<E> = emptySet(),
    states: Set<S> = emptySet()
) : Executor<D, E, S>(machine, events, states) {

    infix fun execAndSet(block: (event: @UnsafeVariance E, state: @UnsafeVariance S) -> @UnsafeVariance S) =
        mapper.addMapping(events, states) { p1, p2 -> block(p1, p2) }

    infix fun set(state: @UnsafeVariance S) = mapper.addMapping(events, states) { _, _ -> state }
}

data class ExecutorScope<out D : IData, out E : IEvent<D>, out S : IState>(
    val event: E?,
    val state: S,
    val data: D?,
    val previousChanges: Set<OnStateChanged<D, E, S>>
)