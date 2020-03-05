package de.gapps.utils.statemachine.scopes

import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

open class Executor<out D : Any>(
    machine: IMachineEx<D>,
    val events: Set<IEvent<D>> = emptySet(),
    val states: Set<IState<D>> = emptySet(),
    val isStateEnter: Boolean = false
) : IMachineEx<D> by machine {

    operator fun plusAssign(state: IState<@UnsafeVariance D>?) =
        mapper.addMapping(events, states) { _, _ -> state }.asUnit()

    operator fun plusAssign(block: suspend ExecutorScope<D>.() -> IState<@UnsafeVariance D>?) =
        mapper.addMapping(events, states) { _, _ -> ExecutorScope(event!!, state).block() ?: state }.asUnit()

    operator fun minusAssign(block: suspend ExecutorScope<D>.() -> Unit) =
        mapper.addMapping(events, states) { _, _ -> ExecutorScope(event!!, state).block(); state }.asUnit()
}

open class FullExecutor<out D : Any>(
    machine: IMachineEx<D>,
    events: Set<IEvent<D>> = emptySet(),
    states: Set<IState<D>> = emptySet()
) : Executor<D>(machine, events, states)

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