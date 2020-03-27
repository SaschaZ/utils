@file:Suppress("UNUSED_PARAMETER")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.misc.ifNull
import de.gapps.utils.statemachine.IConditionElement.*
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType

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
            conditions.filter { it.value.isEventCondition && match(it.value, event, state, previousChanges) }
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
                "\tnewState=$newState;" +
                        "\n\tmatchingEventConditions=${matchingEventConditions.toList().joinToStringTabbed(2)}"
            )

        return newState?.also {
            val matchingStateConditions = conditions.filter {
                it.value.isStateCondition && match(it.value, event, newState, previousChanges)
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
            if (!event.noLogging)
                Log.i(
                    "No new state for event $event and $state. Had ${matchingEventConditions.size}" +
                            " matches:${matchingEventConditions.values.joinToStringTabbed(2)}"
                )
            null
        }
    }

    fun match(
        condition: ICondition,
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>
    ): Boolean {
        fun match(runtime: ISlave, definition: ISlave): Boolean {
            return when (runtime) {
                is IType<*> -> when (definition) {
                    is IType<*> -> runtime.type == definition.type
                    is IData -> runtime.type.isInstance(definition)
                    else -> throw IllegalArgumentException("Unknown ISlave subtype $definition.")
                }
                is IData -> when (definition) {
                    is IType<*> -> definition.type.isInstance(runtime)
                    is IData -> runtime == definition
                    else -> throw IllegalArgumentException("Unknown ISlave subtype $definition.")
                }
                else -> throw IllegalArgumentException("Unknown ISlave subtype $runtime.")
            }
        }

        fun match(runtime: IComboElement, definition: IComboElement): Boolean {
            return runtime.master == definition.master
                    && ((runtime.slave == null && definition.slave == null) || runtime.ignoreSlave || definition.ignoreSlave
                    || (runtime.slave != null && definition.slave != null && match(
                runtime.slave!!,
                definition.slave!!
            )))
        }

        fun List<IComboElement>.matchAny(runtime: IComboElement) = isEmpty() || any { match(runtime, it) }
        fun List<IComboElement>.matchAll(runtime: IComboElement) = isEmpty() || all { match(runtime, it) }
        fun List<IComboElement>.matchNone(runtime: IComboElement) = isEmpty() || none { match(runtime, it) }

        return when (condition.type) {
            EVENT -> condition.wantedEventsAny.matchAny(event)
                    && condition.unwantedEvents.matchNone(event)
                    && condition.wantedStatesAny.matchAny(state)
                    && condition.wantedStatesAll.matchAll(state)
                    && condition.unwantedStates.matchNone(state)

            STATE -> condition.wantedStatesAny.matchAny(state)
                    && condition.unwantedStates.matchNone(state)
                    && condition.wantedEventsAny.matchAny(event)
                    && condition.wantedEventsAll.matchAll(event)
                    && condition.unwantedEvents.matchNone(event)
        }.also {
            if (!event.noLogging) logV {
                @Suppress("RemoveCurlyBracesFromTemplate")
                m = "#1 $it => $condition <==> ${when (condition.type) {
                    EVENT -> state
                    STATE -> event
                }}"
            }
        }
    }
}