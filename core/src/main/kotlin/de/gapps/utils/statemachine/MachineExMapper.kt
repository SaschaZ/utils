@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import kotlin.random.Random

interface IMachineExMapper {

    data class DataToActionEntry(
        val possibleEvents: Set<IEvent>,
        val possibleStates: Set<IState>,
        val action: (event: IEvent, state: IState) -> IState?
    )

    val dataToActionMap: MutableMap<Long, DataToActionEntry>

    fun addMapping(
        events: Set<IEvent>,
        states: Set<IState>,
        action: (event: IEvent, state: IState) -> IState?
    ): Long {
        val newId = dataToActionMap.newId
        dataToActionMap[newId] = DataToActionEntry(events, states, action)
        return newId
    }

    val stateActionMap: MutableMap<Long, Pair<Set<IState>, (event: IEvent, state: IState) -> Unit>>

    fun addMapping(
        states: Set<IState>,
        action: (event: IEvent, state: IState) -> Unit
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
        event: @UnsafeVariance IEvent,
        state: @UnsafeVariance IState,
        previousChanges: Set<OnStateChanged>
    ): IState? {
        val mapped = dataToActionMap.filter {
            it.value.possibleEvents.contains(event)
                    && it.value.possibleStates.contains(state)
        }.mapNotNull { it.value.action(event, state) }
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

class MachineExMapper : IMachineExMapper {
    override val dataToActionMap:
            MutableMap<Long, IMachineExMapper.DataToActionEntry> =
        HashMap()

    override val stateActionMap: MutableMap<Long, Pair<Set<IState>, (event: IEvent, state: IState) -> Unit>> =
        HashMap()

}