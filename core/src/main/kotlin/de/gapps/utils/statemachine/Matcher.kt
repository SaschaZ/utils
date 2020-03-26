@file:Suppress("UNUSED_PARAMETER")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.statemachine.IConditionElement.ICondition
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster
import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.IState

object Matcher {

    /**
     * Is called to determine the next state when a new event is processed.
     *
     * @param event new event
     * @param state current state
     * @param previousChanges previous state changes of the state machine
     * @return new state
     */
    suspend fun findStateForEvent(
        event: IEvent,
        state: IState,
        previousChanges: List<OnStateChanged>,
        conditions: Map<Long, ICondition>
    ): IState? {
        if (event.noLogging)
            Log.v(
                "findStateForEvent()\n\tevent=$event;\n\tstate=$state;\n\t" +
                        "previousChanges=${previousChanges.toList().takeLast(3).joinToStringTabbed(2)}"
            )

        val execScope = ExecutorScope(event, state, previousChanges)

        val matchingEventConditions =
            conditions.filter { it.value.isEventCondition && it.value.match(event, state, previousChanges) }
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
                it.value.isStateCondition && it.value.match(event, state, previousChanges)
            }

            if (!event.noLogging)
                Log.v(
                    "executing matching state conditions: \n${matchingStateConditions.entries.joinToStringTabbed(
                        2
                    )}"
                )

            matchingStateConditions.forEach { it.value.action?.invoke(execScope) }

            Log.d("state changed from $state to $newState with event $event")
        }
    }

    private fun ICondition.match(
        event: IEvent,
        state: IState,
        previousChanges: List<OnStateChanged>,
        toMatch: IMaster = when (type) {
            EVENT -> event
            STATE -> state
        }
    ): Boolean {
        val wantedStatesAll = wantedStates.filter { it.idx > 0 }
        val wantedStatesAny = wantedStates.filter { it.idx == 0 }
        val wantedEventsAll = wantedEvents.filter { it.idx > 0 }
        val wantedEventsAny = wantedEvents.filter { it.idx == 0 }

        return when (type) {
            EVENT -> start.match(event, state, type, previousChanges)
                    && (wantedStatesAny.isEmpty() || wantedStatesAny.any {
                it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
                    && (wantedStatesAll.isEmpty() || wantedStatesAll.all {
                it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
                    && (unwantedStates.isEmpty() || unwantedStates.all {
                !it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
            STATE -> start.match(event, state, type, previousChanges)
                    && (wantedEventsAny.isEmpty() || wantedEventsAny.any {
                it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
                    && (wantedEventsAll.isEmpty() || wantedEventsAll.all {
                it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
                    && (unwantedEvents.isEmpty() || unwantedEvents.all {
                !it.match(
                    event,
                    state,
                    type,
                    previousChanges
                )
            })
        } logV {
            @Suppress("RemoveCurlyBracesFromTemplate")
            m = "\t$it => ${this@match} <==> $toMatch"
        }
    }

    @Suppress("RemoveCurlyBracesFromTemplate")
    private fun IMaster.match(
        event: IEvent,
        state: IState,
        type: ConditionType,
        previousChanges: List<OnStateChanged>,
        second: IMaster = when (type) {
            EVENT -> event
            STATE -> state
        }
    ): Boolean {
        return false logV { m = "$it => ${this@match} <==> $second" }
    }
}
