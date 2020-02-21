package de.gapps.utils.statemachine.scopes

import de.gapps.utils.log.Log
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

open class MachineExScope<out E : IEvent<*, *>, out S : IState> : IMachineExScope<E, S> {

    private val eventStatePairToEventChangeScopeMap = HashMap<Pair<@UnsafeVariance E, @UnsafeVariance S>,
            EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S>()

    val eventStateMapping
        get() = HashMap(eventStatePairToEventChangeScopeMap)

    private val stateToStateChangeScopeMap =
        HashMap<@UnsafeVariance S, StateChangeScope<@UnsafeVariance S>.() -> Unit>()

    override fun addMapping(
        events: List<@UnsafeVariance E>,
        states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) {
        events.forEach { event ->
            states.forEach { state ->
                eventStatePairToEventChangeScopeMap.containsKey(event to state)
                        && throw IllegalArgumentException("Action for event $event and state $state already defined.")
                eventStatePairToEventChangeScopeMap[event to state] = action
            }
        }
    }

    override fun addMapping(states: List<@UnsafeVariance S>, action: StateChangeScope<@UnsafeVariance S>.() -> Unit) {
        states.forEach { state ->
            if (stateToStateChangeScopeMap[state] != null) Log.w("Overwriting state action for state $state")
            stateToStateChangeScopeMap[state] = action
        }
    }
}