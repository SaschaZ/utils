package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

open class Executor<out D : Any>(
    machine: IMachineEx<D>,
    val events: Set<IEvent<D>> = emptySet(),
    val states: Set<IState<D>> = emptySet(),
    val isStateEnter: Boolean = false
) : IMachineEx<D> by machine {

    infix fun exec(block: suspend ExecutorScope<D>.() -> Unit) =
        mapper.addMapping(states) { event, state -> ExecutorScope(event, state).block() }
}

open class FullExecutor<out D : Any>(
    machine: IMachineEx<D>,
    events: Set<IEvent<D>> = emptySet(),
    states: Set<IState<D>> = emptySet()
) : Executor<D>(machine, events, states) {

    infix fun execAndSet(block: suspend ExecutorScope<D>.() -> IState<@UnsafeVariance D>) =
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }

    infix fun set(state: IState<@UnsafeVariance D>) = mapper.addMapping(events, states) { _, _ -> state }
}

data class ExecutorScope<out D : Any>(
    val event: IEvent<D>,
    val state: IState<D>
) {
    val eventData: D? = event.data
    val stateData: D? = state.data

    @Suppress("UNCHECKED_CAST")
    fun <D : Any> eventData() = eventData as? D

    @Suppress("UNCHECKED_CAST")
    fun <D : Any> stateData() = stateData as? D
}

operator fun <D : Any> Set<IState<D>>.plus(data: D): Set<IState<D>> {
    forEach { it.data = data }
    return this
}

operator fun <D : Any> IState<D>.plus(data: D): IState<D> {
    this.data = data
    return this
}

operator fun <D : Any> IState<D>.plusAssign(other: Any) {

}