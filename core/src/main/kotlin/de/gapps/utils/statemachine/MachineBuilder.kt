package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.misc.ifN
import de.gapps.utils.statemachine.scopes.MachineExScope
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope

/**
 * Builder for MachineEx. Allows building of event action mapping with a simple DSL instead of providing it in a List.
 */
inline fun <reified E : IEvent, reified S : IState> machineEx(
    initialState: S,
    builder: MachineExScope<E, S>.() -> Unit
): MachineEx<E, S> {
    return MachineExScope<E, S>().run {
        builder()
        MachineEx(initialState) { e ->
            EventChangeScope(event, state).run rt@{
                val fittingPair = eventStateMapping.entries.firstOrNull {
                    it.key.run {
                        if (false) e::class.isInstance(first) else e == first
                                && if (false) state::class.isInstance(second) else state == second
                    }
                }
                fittingPair?.value?.invoke(this) logV { m = "$this -> $it" } ifN {
                    Log.w("No state defined for event $event with state $state.")
                    state
                }
            }
        }
    }
}