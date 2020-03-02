package de.gapps.utils.statemachine

import de.gapps.utils.log.Log

interface IMachineExMapper<out D : IData, out E : IEvent, out S : IState> {

    data class DataToActionEntry<out D : IData, out E : IEvent, out S : IState>(
        val possibleEvents: Set<E>,
        val possibleStates: Set<S>,
        val action: () -> S?
    )

    val dataToActionMap: MutableList<DataToActionEntry<@UnsafeVariance D, @UnsafeVariance E, @UnsafeVariance S>>

    fun addMapping(
        events: Set<@UnsafeVariance E>,
        states: Set<@UnsafeVariance S>,
        action: () -> @UnsafeVariance S?
    ) = dataToActionMap.add(DataToActionEntry(events, states, action))

    val stateActionMap: MutableMap<Set<@UnsafeVariance S>, () -> Unit>

    fun addMapping(
        states: Set<@UnsafeVariance S>,
        action: () -> Unit
    ) {
        stateActionMap[states] = action
    }

    /**
     * Is called to determine the next state when a new event is processed.
     *
     * @param event new event
     * @param state current state
     * @param previousChanges previous state changes of the state machine
     * @return new state
     */
    suspend fun findStateForEvent(
        event: @UnsafeVariance E,
        state: @UnsafeVariance S,
        previousChanges: Set<OnStateChanged>
    ): S? {
        val mapped = dataToActionMap.filter { it.possibleEvents.contains(event) && it.possibleStates.contains(state) }
            .mapNotNull { it.action() }
        stateActionMap.filter { it.key.contains(state) }.values.forEach { it() }
        return when {
            mapped.size < 2 -> mapped.getOrNull(0)
            else -> {
                Log.v("No action defined for $event and $state with ${previousChanges.joinToString()}")
                null
            }
        }
    }
}

class MachineExMapper<out D: IData, out E: IEvent, out S: IState> : IMachineExMapper<D, E, S> {
    override val dataToActionMap: MutableList<
            IMachineExMapper.DataToActionEntry<@UnsafeVariance D, @UnsafeVariance E, @UnsafeVariance S>> = ArrayList()

    override val stateActionMap: MutableMap<Set<@UnsafeVariance S>, () -> Unit> = HashMap()

}