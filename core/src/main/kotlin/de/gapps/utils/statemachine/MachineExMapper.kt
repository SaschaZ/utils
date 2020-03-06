@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.misc.runEach

interface IMachineExMapper {

    data class DataToActionEntry(
        val possibleEvents: Set<ValueDataHolder<Event>>,
        val possibleStates: Set<ValueDataHolder<State>>,
        val action: suspend (event: ValueDataHolder<Event>, state: ValueDataHolder<State>) -> ValueDataHolder<State>?
    )

    val dataToActionMap: MutableMap<Long, DataToActionEntry>

    fun addMapping(
        events: Set<ValueDataHolder<Event>>,
        states: Set<ValueDataHolder<State>>,
        action: suspend (event: ValueDataHolder<Event>, state: ValueDataHolder<State>) -> ValueDataHolder<State>?
    ): Long = if (events.isEmpty()) addMapping(states) { event, state -> action(event, state) }
    else {
        newId.also {
            Log.v("$it =>\n\tevents=$events\n\tstates=$states\n\taction=$action")
            dataToActionMap[it] = DataToActionEntry(events, states, action)
        }
    }

    data class StateActionEntry(
        val possibleStates: Set<ValueDataHolder<State>>,
        val action: suspend (event: ValueDataHolder<Event>, state: ValueDataHolder<State>) -> Unit
    )

    val stateActionMap: MutableMap<Long, StateActionEntry>

    fun addMapping(
        states: Set<ValueDataHolder<State>>,
        action: suspend (
            event: ValueDataHolder<Event>,
            state: ValueDataHolder<State>
        ) -> Unit
    ): Long = newId.also {
        Log.v("$it =>\n\tstates=$states\n\taction=$action")
        stateActionMap[it] = StateActionEntry(states, action)
    }

    var lastId: Long

    private val newId: Long
        get() = ++lastId

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
        event: ValueDataHolder<Event>,
        state: ValueDataHolder<State>,
        previousChanges: Set<OnStateChanged>
    ): ValueDataHolder<State>? {
        Log.v("findStateForEvent()\n\tevent=$event; state=$state;\n\tpreviousChanges=${previousChanges.joinToString()}")

        val filteredDataActions = dataToActionMap.filter {
            event isOneOf it.value.possibleEvents && state isOneOf it.value.possibleStates
        }
        val mappedEvents = filteredDataActions.mapNotNull { it.value.action(event, state) }

        val newState = when (mappedEvents.size) {
            in 0..1 -> mappedEvents.firstOrNull()
            else -> throw IllegalStateException("To much states defined for $event and $state")
        }

        val filteredStates = stateActionMap.filter { newState isOneOf it.value.possibleStates }.values
        filteredStates.runEach { action(event, state) }

        println(
            "\tnewState=$newState;" +
                    "\n\tfilteredDataActions=${filteredDataActions.toList().joinToString()}" +
                    "\n\tfilteredStates=${filteredStates.joinToString()}"
        )
        return newState
    }
}

class MachineExMapper : IMachineExMapper {

    override val dataToActionMap: MutableMap<Long, IMachineExMapper.DataToActionEntry> = HashMap()
    override val stateActionMap: MutableMap<Long, IMachineExMapper.StateActionEntry> = HashMap()
    override var lastId: Long = -1L
}

infix fun <T : Any> T?.isOneOf(list: Collection<T>) = list.contains(this)