@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import kotlin.random.Random

interface IMachineExMapper<out D : Any> {

    data class DataToActionEntry<out D : Any>(
        val possibleEvents: Set<IEvent<@UnsafeVariance D>>,
        val possibleStates: Set<IState<@UnsafeVariance D>>,
        val action: suspend (event: IEvent<@UnsafeVariance D>, state: IState<@UnsafeVariance D>) -> IState<D>?
    )

    val dataToActionMap: MutableMap<Long, DataToActionEntry<@UnsafeVariance D>>

    fun addMapping(
        events: Set<IEvent<@UnsafeVariance D>>,
        states: Set<IState<@UnsafeVariance D>>,
        action: suspend (event: IEvent<D>, state: IState<D>) -> IState<@UnsafeVariance D>?
    ): Long {
        val newId = dataToActionMap.newId
        dataToActionMap[newId] = DataToActionEntry(events, states, action)
        return newId
    }

    val stateActionMap: MutableMap<Long, Pair<Set<IState<@UnsafeVariance D>>,
            suspend (
                event: IEvent<@UnsafeVariance D>,
                state: IState<@UnsafeVariance D>
            ) -> Unit>>

    fun addMapping(
        states: Set<IState<@UnsafeVariance D>>,
        action: suspend (event: IEvent<D>, state: IState<D>) -> Unit
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
        event: @UnsafeVariance IEvent<@UnsafeVariance D>,
        state: @UnsafeVariance IState<@UnsafeVariance D>,
        previousChanges: Set<OnStateChanged<@UnsafeVariance D>>
    ): IState<D>? {
        val mappedEvents = dataToActionMap.filter {
            event isOneOf it.value.possibleEvents && state isOneOf it.value.possibleStates
        }.mapNotNull { it.value.action(event, state) }

        val newState = when {
            mappedEvents.size in 1..2 -> mappedEvents.firstOrNull()
            mappedEvents.isEmpty() -> {
                Log.v("No action defined for $event and $state with ${previousChanges.joinToString()}")
                null
            }
            else -> throw IllegalStateException("To much states defined for $event and $state")
        }

        stateActionMap.filter { newState == it.value.first }.values.forEach { it.second(event, state) }

        return newState
    }
}

class MachineExMapper<out D : Any> : IMachineExMapper<D> {
    override val dataToActionMap: MutableMap<Long, IMachineExMapper.DataToActionEntry<@UnsafeVariance D>> = HashMap()

    override val stateActionMap: MutableMap<Long, Pair<Set<IState<@UnsafeVariance D>>,
            suspend (
                event: IEvent<@UnsafeVariance D>,
                state: IState<@UnsafeVariance D>
            ) -> Unit>> =
        HashMap()

}

infix fun <T : Any> T.isOneOf(list: Collection<T>) = list.contains(this)