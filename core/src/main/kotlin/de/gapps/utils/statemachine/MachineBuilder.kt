package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
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
            var resultState: S? = null
            val matches = dataToExecutionMap.entries.filter { it.key.matches(e, state, previousChanges) }
            when {
                matches.size > 1 ->
                    Log.w("More than one match found for event $event with state $state (\n${matches.joinToString("\n")}).")
                matches.size == 1 -> matches.first().value.execute()?.resultingState?.also { newState ->
                    resultState = newState
                }
                matches.isEmpty() -> Log.d("No state defined for event $event with state $state.")
            }
            resultState
        }
        MachineEx(initialState, this@run, findStateForEvent = findStateForEvent).also {
            StateMachineHolder<E, S>(it).builder()
        }
    }
}