@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.statemachine.scopes.ExecutorScope

/**
 * Responsible to map the incoming [Event]s to their [State]s defined by provided mappings.
 */
interface IMachineExMapper {

    /**
     *
     *
     * @property possibleEvents
     * @property possibleStates
     * @property isStateCondition
     * @property action
     */
    data class ConditionToActionEntry(
        val possibleEvents: Set<ValueDataHolder>,
        val possibleStates: Set<ValueDataHolder>,
        val isStateCondition: Boolean = false,
        val action: suspend ExecutorScope.() -> ValueDataHolder?
    ) {

        /**
         *
         */
        fun match(event: ValueDataHolder, state: ValueDataHolder): Boolean {
            val isEventCondition = !isStateCondition
            val areWantedEventsEmpty = possibleEvents.wanted.isEmpty()
            val areWantedStatesEmpty = possibleStates.wanted.isEmpty()
            val result = (isStateCondition && areWantedEventsEmpty
                    || event isOneOf possibleEvents.wanted)
                    && (isEventCondition && areWantedStatesEmpty
                    || state isOneOf possibleStates.wanted)
                    && event isNoneOf possibleEvents.unwanted
                    && state isNoneOf possibleStates.unwanted
            return result
        }
    }

    val conditionToActionMap: MutableMap<Long, ConditionToActionEntry>

    /**
     *
     */
    fun addMapping(
        entryBuilder: EntryBuilder,
        action: suspend ExecutorScope.() -> ValueDataHolder?
    ): Long = newId.also { id ->
        entryBuilder.run {
            Log.v(
                "$id => isStateCondition=$isStateCondition" +
                        "\n\tevents=$events\n\tstates=$states\n\taction=$action"
            )
            conditionToActionMap[id] = ConditionToActionEntry(events, states, isStateCondition, action)
        }
    }

    var lastId: Long

    private val newId: Long
        get() = ++lastId

    /**
     *
     */
    fun removeMapping(id: Long) = conditionToActionMap.remove(id)

    /**
     * Is called to determine the next state when a new event is processed.
     *
     * @param event new event
     * @param state current state
     * @param previousChanges previous state changes of the state machine
     * @return new state
     */
    suspend fun findStateForEvent(
        event: ValueDataHolder,
        state: ValueDataHolder,
        previousChanges: Set<OnStateChanged>
    ): ValueDataHolder? {
        Log.v(
            "findStateForEvent()\n\tevent=$event; state=$state;\n\t" +
                    "previousChanges=${previousChanges.joinToStringTabbed(2)}"
        )

        return ExecutorScope(event, state).run {
            val filteredDataActions = conditionToActionMap
                .filter { !it.value.isStateCondition && it.value.match(event, state) }
            val mappedEvents = filteredDataActions.mapNotNull { it.value.run { action() } }

            val newState = when (mappedEvents.size) {
                in 0..1 -> mappedEvents.firstOrNull()
                else -> throw IllegalStateException("To much states defined for $event and $state")
            }

            newState?.also {
                conditionToActionMap.filter {
                    it.value.isStateCondition
                            && it.value.match(event, newState)
                            && it.value.run { action() } != null
                }
            }

            println(
                "\tnewState=$newState;" +
                        "\n\tfilteredDataActions=t${filteredDataActions.toList().joinToStringTabbed(2)}"
            )
            newState
        }
    }
}

class MachineExMapper : IMachineExMapper {

    override val conditionToActionMap: MutableMap<Long, IMachineExMapper.ConditionToActionEntry> = HashMap()
    override var lastId: Long = -1L
}

infix fun <T : ValueDataHolder> T?.isOneOf(list: Collection<T>): Boolean =
    list.contains(this)

infix fun <T : ValueDataHolder> T?.isNoneOf(list: Collection<T>): Boolean =
    !list.contains(this)

val <T : ValueDataHolder> Set<T>.wanted: Set<T>
    get() = filter { !it.exclude }.toSet()

val <T : ValueDataHolder> Set<T>.unwanted: Set<T>
    get() = filter { it.exclude }.toSet()