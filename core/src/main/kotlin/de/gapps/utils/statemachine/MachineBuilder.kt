package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.scopes.MachineExScope
import de.gapps.utils.statemachine.scopes.definition.IStateMachineHolder
import de.gapps.utils.statemachine.scopes.definition.StateMachineHolder

/**
 * Builder for MachineEx. Allows building of event action mapping with a simple DSL instead of providing it in a List.
 */
inline fun <reified E : IEvent, reified S : IState> machineEx(
    initialState: S,
    builder: IStateMachineHolder<E, S>.() -> Unit
): MachineEx<E, S> {
    return MachineExScope<E, S>().run {
        val findStateForEvent: suspend IMachineEx<E, S>.(E) -> S? = { e ->
            val matches = dataToExecutionMap.entries
                .filter { it.key.matches(e, state, previousChanges) }
                .mapNotNull { it.value.execute()?.resultingState }
            val resultState = when (matches.size) {
                0, 1 -> matches.firstOrNull()
                else -> throw IllegalStateException("Matches contain more than one resulting state.")
            }
            resultState
        }
        MachineEx(initialState, this@run, findStateForEvent = findStateForEvent).also {
            StateMachineHolder(it).builder()
        }
    }
}