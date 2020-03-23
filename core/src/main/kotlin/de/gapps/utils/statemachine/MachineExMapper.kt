@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.misc.runEach
import de.gapps.utils.statemachine.ConditionElement.CombinedConditionElement
import de.gapps.utils.statemachine.ConditionElement.Condition
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs.DEFINITION

/**
 * Responsible to map the incoming [Event]s to their [State]s defined by provided mappings.
 */
interface IMachineExMapper {

    val conditions: MutableMap<Long, Condition>

    /**
     *
     */
    fun addCondition(
        condition: Condition,
        action: suspend ExecutorScope.() -> ICombinedConditionElement?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition")
        condition.wanted.runEach { usedAs = DEFINITION }
        condition.unwanted.runEach { usedAs = DEFINITION }
        conditions[id] = condition.copy(action = action)
    }

    var lastId: Long

    private val newId: Long
        get() = ++lastId

    /**
     *
     */
    fun removeMapping(id: Long) = conditions.remove(id)

    /**
     * Is called to determine the next state when a new event is processed.
     *
     * @param event new event
     * @param state current state
     * @param previousChanges previous state changes of the state machine
     * @return new state
     */
    suspend fun findStateForEvent(
        event: ICombinedConditionElement,
        state: ICombinedConditionElement,
        previousChanges: Set<OnStateChanged>
    ): ICombinedConditionElement? {
        Log.v(
            "findStateForEvent()\n\tevent=$event;\n\tstate=$state;\n\t" +
                    "previousChanges=${previousChanges.joinToStringTabbed(2)}"
        )

        val newState = ExecutorScope(event, state, previousChanges).run {
            MatchScope(event, state, previousChanges).run {
                val matchingEventConditions =
                    conditions.filter { it.value.isEventCondition && it.value.run { match() } }
                val matchedResults = matchingEventConditions.mapNotNull { it.value.run { action() } }

                val newState = when (matchedResults.size) {
                    in 0..1 -> matchedResults.firstOrNull()
                    else -> throw IllegalStateException(
                        "To much states defined for $event and $state " +
                                "with mappedEvents=${matchedResults.joinToStringTabbed()}"
                    )
                }

                Log.v(
                    "\tnewState=$newState;" +
                            "\n\tmatchingEventConditions=t${matchingEventConditions.toList().joinToStringTabbed(2)}"
                )

                newState
            }
        }

        return newState?.also {
            ExecutorScope(event, newState, previousChanges).run {
                MatchScope(event, newState, previousChanges).run {
                    val matchingStateConditions = conditions.filter {
                        it.value.isStateCondition && it.value.run { match() }
                    }
                    Log.v("executing matching state conditions: \n${matchingStateConditions.entries.joinToStringTabbed(2)}")
                    matchingStateConditions.forEach { it.value.run { action() } }
                }
            }
        }
    }
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, Condition> = HashMap()
    override var lastId: Long = -1L
}

infix fun CombinedConditionElement?.isOneOf(list: Collection<CombinedConditionElement>): Boolean =
    list.contains(this)

infix fun CombinedConditionElement?.isNoneOf(list: Collection<CombinedConditionElement>): Boolean =
    !list.contains(this)