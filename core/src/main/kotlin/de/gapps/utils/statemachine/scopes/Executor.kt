package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

open class Executor(
    machine: IMachineEx,
    val events: Set<IEvent> = emptySet(),
    val states: Set<IState> = emptySet(),
    val isStateEnter: Boolean = false
) : IMachineEx by machine {

    infix fun exec(block: suspend ExecutorScope.() -> Unit) =
        mapper.addMapping(states) { event, state -> ExecutorScope(event, state).block() }
}

open class FullExecutor(
    machine: IMachineEx,
    events: Set<IEvent> = emptySet(),
    states: Set<IState> = emptySet()
) : Executor(machine, events, states) {

    infix fun execAndSet(block: suspend ExecutorScope.() -> IState) =
        mapper.addMapping(events, states) { event, state -> ExecutorScope(event, state).block() }

    infix fun set(state: IState) = mapper.addMapping(events, states) { _, _ -> state }
}

data class ExecutorScope(
    val event: IEvent,
    val state: IState
) {
    val data: IData? = event.data
}