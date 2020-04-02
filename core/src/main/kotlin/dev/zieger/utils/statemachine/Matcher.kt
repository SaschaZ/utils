@file:Suppress("UNUSED_PARAMETER")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.statemachine.ConditionElement.InputElement
import dev.zieger.utils.statemachine.IConditionElement.IComboElement
import dev.zieger.utils.statemachine.IConditionElement.ICondition
import dev.zieger.utils.statemachine.IConditionElement.ICondition.ConditionType
import dev.zieger.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import dev.zieger.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE

/**
 * Holds methods that are used for matching the incoming events to a new state and/or action and to match the new state to actions.
 */
object Matcher {

    /**
     * Is called to determine the next state when a new event is processed.
     * Also executes possible actions.
     *
     * @param event new event
     * @param state current state
     * @param previousChanges previous state changes of the state machine
     * @return new state
     */
    suspend fun findStateForEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>,
        conditions: Map<Long, ICondition>
    ): IComboElement? {
        if (!event.noLogging)
            Log.v(
                "findStateForEvent()\n\tevent=$event;\n\tstate=$state;\n\t" +
                        "previousChanges=${previousChanges.toList().takeLast(3).joinToStringTabbed(2)}"
            )

        val execScope = ExecutorScope(event, state, previousChanges)

        val matchingEventConditions =
            conditions.filter { match(it.value, event, state, previousChanges, EVENT) }
        if (matchingEventConditions.isEmpty() && !event.noLogging) {
            Log.i("No event condition matches for $event and $state.")
            return null
        }
        val matchedResults = matchingEventConditions.mapNotNull { it.value.action?.invoke(execScope) }

        val newState = when (matchedResults.size) {
            in 0..1 -> matchedResults.firstOrNull()
            else -> throw IllegalStateException(
                "To much states defined for $event and $state " +
                        "with mappedEvents=${matchedResults.joinToStringTabbed()}"
            )
        }

        if (!event.noLogging)
            Log.v(
                "\n\tnewState=$newState" +
                        "\n\tmatchingEventConditions=${matchingEventConditions.toList().joinToStringTabbed(2)}"
            )

        return newState?.also {
            val matchingStateConditions = conditions.filter {
                match(it.value, event, newState, previousChanges, STATE)
            }

            if (!event.noLogging)
                Log.v(
                    "executing matching state conditions: \n" +
                            matchingStateConditions.entries.joinToStringTabbed(2)
                )

            matchingStateConditions.forEach { it.value.action?.invoke(execScope) }

            if (!event.noLogging)
                Log.d("state changed from $state to $newState with event $event")
        } ifNull {
            if (!event.noLogging && matchingEventConditions.values.isEmpty())
                Log.i(
                    "No event condition matches for $event and $state. Had ${matchingEventConditions.size}" +
                            " matches:${matchingEventConditions.values.joinToStringTabbed(2)}"
                )
            null
        }
    }

    private fun match(
        condition: ICondition,
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>,
        type: ConditionType
    ) = (condition.type == type && condition.match(InputElement(event, state), previousChanges)) logV
            { m = "#R $it => ${type.name[0]} $condition <||> $event, $state" }
}