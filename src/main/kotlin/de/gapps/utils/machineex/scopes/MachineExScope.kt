package de.gapps.utils.machineex.scopes

import de.gapps.utils.machineex.IEvent
import de.gapps.utils.machineex.IState
import de.gapps.utils.misc.Log

open class MachineExScope<out E : IEvent, out S : IState> : IMachineExScope<E, S> {

    internal val eventStatesActionMapping = HashMap<Pair<@UnsafeVariance E, @UnsafeVariance S>,
            EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S>()
    internal val stateEventStateActionMap = HashMap<@UnsafeVariance S, StateChangeScope<@UnsafeVariance S>.() -> Unit>()

    override fun addMapping(
        events: List<@UnsafeVariance E>, states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) {
        events.forEach { event ->
            states.forEach { state ->
                if (eventStatesActionMapping[event to state] != null)
                    Log.w("Overwriting action for event $event and state $state")
                eventStatesActionMapping[event to state] = action
            }
        }
    }

    override fun addMapping(states: List<@UnsafeVariance S>, action: StateChangeScope<@UnsafeVariance S>.() -> Unit) {
        states.forEach { state ->
            if (stateEventStateActionMap[state] != null) Log.w("Overwriting state action for state $state")
            stateEventStateActionMap[state] = action
        }
    }
}