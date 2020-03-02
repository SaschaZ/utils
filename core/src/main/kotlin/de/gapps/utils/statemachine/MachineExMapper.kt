package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import kotlin.random.Random

interface IMachineExMapper<out D : IData, out E : IEvent<D>, out S : IState> {

    data class DataToActionEntry<out D : IData, out E : IEvent<D>, out S : IState>(
        val possibleEvents: Set<E>,
        val possibleStates: Set<S>,
        val action: (event: @UnsafeVariance E, state: @UnsafeVariance S) -> S?
    )

    val dataToActionMap: MutableMap<Long, DataToActionEntry<@UnsafeVariance D, @UnsafeVariance E, @UnsafeVariance S>>

    fun addMapping(
        events: Set<@UnsafeVariance E>,
        states: Set<@UnsafeVariance S>,
        action: (event: @UnsafeVariance E, state: @UnsafeVariance S) -> @UnsafeVariance S?
    ): Long {
        val newId = dataToActionMap.newId
        dataToActionMap[newId] = DataToActionEntry(events, states, action)
        return newId
    }

    val stateActionMap: MutableMap<Long, Pair<Set<@UnsafeVariance S>, (event: @UnsafeVariance E, state: @UnsafeVariance S) -> Unit>>

    fun addMapping(
        states: Set<@UnsafeVariance S>,
        action: (event: @UnsafeVariance E, state: @UnsafeVariance S) -> Unit
    ): Long {
        val newId = stateActionMap.newId
        stateActionMap[newId] = states to action
        return newId
    }

    private val MutableMap<Long, *>.newId: Long
        get() {
            var newId = Random.nextLong()
            while (containsKey(newId)) newId = Random.nextLong()
            return newId
        }

    fun removeEventMapping(id: Long) = dataToActionMap.remove(id)

    fun removeStateMapping(id: Long) = stateActionMap.remove(id)

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
        previousChanges: Set<OnStateChanged<@UnsafeVariance D, @UnsafeVariance E, @UnsafeVariance S>>
    ): S? {
        val mapped = dataToActionMap.filter {
            it.value.possibleEvents.contains(event)
                    && it.value.possibleStates.contains(state)
        }
            .mapNotNull { it.value.action(event, state) }
        return when {
            mapped.size < 2 -> mapped.firstOrNull()
            else -> {
                Log.v("No action defined for $event and $state with ${previousChanges.joinToString()}")
                null
            }
        }?.also { newState ->
            stateActionMap.filter { it.value.first.contains(newState) }.values.forEach { it.second(event, state) }
        }
    }
}

class MachineExMapper<out D : IData, out E : IEvent<D>, out S : IState> : IMachineExMapper<D, E, S> {
    override val dataToActionMap:
            MutableMap<Long, IMachineExMapper.DataToActionEntry<@UnsafeVariance D, @UnsafeVariance E, @UnsafeVariance S>> =
        HashMap()

    override val stateActionMap: MutableMap<Long, Pair<Set<@UnsafeVariance S>, (event: @UnsafeVariance E, state: @UnsafeVariance S) -> Unit>> =
        HashMap()

}