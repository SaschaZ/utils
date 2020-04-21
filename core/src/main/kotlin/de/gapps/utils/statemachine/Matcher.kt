@file:Suppress("UNUSED_PARAMETER")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.log.LogFilter.Companion.GENERIC
import de.gapps.utils.log.logV
import de.gapps.utils.misc.ifNull
import de.gapps.utils.statemachine.ConditionElement.InputElement
import de.gapps.utils.statemachine.IConditionElement.IComboElement
import de.gapps.utils.statemachine.IConditionElement.ICondition
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
import de.gapps.utils.statemachine.MachineEx.Companion.DebugLevel.INFO

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
        Log.v(
            "findStateForEvent()\n\tevent=$event;\n\tstate=$state;\n\t" +
                    "previousChanges=${previousChanges.toList().take(3).joinToStringTabbed(2)}",
            logFilter = GENERIC(
                disableLog = event.disableLogging || MachineEx.debugLevel <= INFO
            )
        )

        val execScope = ExecutorScope(event, state, previousChanges)

        val matchingEventConditions =
            conditions.filter { match(it.value, event, state, previousChanges, EVENT) }
        if (matchingEventConditions.isEmpty()) {
            Log.i(
                "No event condition matches for $event and $state.",
                GENERIC(disableLog = event.disableLogging || MachineEx.debugLevel == ERROR)
            )
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

        Log.v(
            "\n\tnewState=$newState" +
                    "\n\tmatchingEventConditions=${matchingEventConditions.toList().joinToStringTabbed(2)}",
            GENERIC(disableLog = event.disableLogging || MachineEx.debugLevel <= INFO)
        )

        return newState?.also {
            val matchingStateConditions = conditions.filter {
                match(it.value, event, newState, previousChanges, STATE)
            }

            Log.v(
                "executing matching state conditions: \n" +
                        matchingStateConditions.entries.joinToStringTabbed(2),
                GENERIC(disableLog = event.disableLogging || MachineEx.debugLevel <= INFO)
            )

            matchingStateConditions.forEach { it.value.action?.invoke(execScope) }

            Log.d(
                "state changed from $state to $newState with event $event",
                GENERIC(disableLog = event.disableLogging || MachineEx.debugLevel == ERROR)
            )
        } ifNull {
            Log.i(
                "No event condition matches for $event and $state. Had ${matchingEventConditions.size}" +
                        " matches:${matchingEventConditions.values.joinToStringTabbed(2)}",
                GENERIC(
                    disableLog = matchingEventConditions.values.isNotEmpty() || event.disableLogging
                            || MachineEx.debugLevel <= INFO
                )
            )
            null
        }
    }

    private suspend fun match(
        condition: ICondition,
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>,
        type: ConditionType
    ) = (condition.type == type && condition.match(InputElement(event, state), previousChanges)) logV
            {
                f = GENERIC(disableLog = event.disableLogging || MachineEx.debugLevel <= INFO)
                m = "#R $it => ${type.name[0]} $condition <||> $event, $state"
            }
}