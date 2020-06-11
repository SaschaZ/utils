@file:Suppress("UNUSED_PARAMETER")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.ICondition
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.STATE
import dev.zieger.utils.statemachine.conditionelements.InputElement
import dev.zieger.utils.statemachine.conditionelements.noLogging

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
     * @param conditions
     * @param bindings
     * @return new state
     */
    suspend fun findStateForEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>,
        conditions: Map<Long, ICondition>,
        bindings: Map<ICondition, IMachineEx>
    ): IComboElement? {
        Log.v(
            "findStateForEvent()\n\tevent=$event;\n\tstate=$state;\n\t" +
                    "previousChanges=${previousChanges.toList().take(3).joinToStringTabbed(2)}",
            logFilter = GENERIC(disableLog = event.noLogging || MachineEx.debugLevel <= INFO)
        )

        val execScope = ExecutorScope(event, state, previousChanges)
        val matchingEventBindings = bindings.filter {
            match(
                it.key, event, state, previousChanges,
                EVENT
            )
        }

        return when (matchingEventBindings.size) {
            1 -> matchingEventBindings.values.first().fire(event).executePossibleStateConditions(
                bindings, event, previousChanges, execScope, conditions, state
            )
            0 -> {
                val matchingEventConditions =
                    conditions.filter { match(it.value, event, state, previousChanges, EVENT) }
                if (matchingEventConditions.isEmpty()) {
                    Log.i(
                        "No event condition matches for $event and $state.",
                        logFilter = GENERIC(disableLog = event.noLogging || MachineEx.debugLevel <= INFO)
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
                    logFilter = GENERIC(disableLog = event.noLogging || MachineEx.debugLevel <= INFO)
                )

                return newState.executePossibleStateConditions(
                    bindings, event, previousChanges, execScope, conditions, state
                ) ifNull {
                    Log.i(
                        "No event condition matches for $event and $state. Had ${matchingEventConditions.size}" +
                                " matches:${matchingEventConditions.values.joinToStringTabbed(2)}",
                        logFilter = GENERIC(
                            disableLog =
                            matchingEventConditions.values.isNotEmpty() || event.noLogging
                                    || MachineEx.debugLevel <= INFO
                        )
                    )
                    null
                }
            }
            else -> throw IllegalStateException("More than one binding found for condition.")
        }
    }

    private suspend fun IComboElement?.executePossibleStateConditions(
        bindings: Map<ICondition, IMachineEx>,
        event: IComboElement,
        previousChanges: List<OnStateChanged>,
        execScope: ExecutorScope,
        conditions: Map<Long, ICondition>,
        state: IComboElement
    ): IComboElement? {
        val newState = this
        return newState?.also {
            bindings.filter { match(it.key, event, newState, previousChanges, STATE) }
                .forEach { it.key.action?.invoke(execScope) }

            val matchingStateConditions = conditions.filter {
                match(it.value, event, newState, previousChanges, STATE)
            }

            Log.v(
                "executing matching state conditions: \n" +
                        matchingStateConditions.entries.joinToStringTabbed(2),
                logFilter = GENERIC(disableLog = event.noLogging || MachineEx.debugLevel <= INFO)
            )

            matchingStateConditions.forEach { it.value.action?.invoke(execScope) }

            Log.d(
                "state changed from $state to $newState with event $event"
            )
        }
    }

    private suspend fun match(
        condition: ICondition,
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>,
        type: ICondition.ConditionType
    ) = (condition.type == type && condition.match(InputElement(event, state), previousChanges)) logV
            {
                logFilter = GENERIC(disableLog = event.noLogging || MachineEx.debugLevel <= INFO)
                "#R $it => ${type.name[0]} $condition <||> $event, $state"
            }
}