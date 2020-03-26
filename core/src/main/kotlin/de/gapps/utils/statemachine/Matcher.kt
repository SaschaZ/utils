@file:Suppress("UNUSED_PARAMETER")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.statemachine.IConditionElement.ICondition
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster
import de.gapps.utils.statemachine.IConditionElement.IMaster.IGroup
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.ISlave.IType
import de.gapps.utils.statemachine.IConditionElement.UsedAs.RUNTIME
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

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
        event: IEvent,
        state: IState,
        previousChanges: List<OnStateChanged>,
        conditions: Map<Long, ICondition>
    ): IState? {
        event.usedAs = RUNTIME

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

            Log.d("state changed from $state to $newState with event $event")
        }
    }

    fun match(
        condition: ICondition,
        event: IEvent,
        state: IState,
        previousChanges: List<OnStateChanged>
    ): Boolean {
        fun List<IMaster>.matchAny() = isEmpty() || any { match(it, event, state, condition.type, previousChanges) }
        fun List<IMaster>.matchAll() = isEmpty() || any { match(it, event, state, condition.type, previousChanges) }

        return when (condition.type) {
            EVENT -> match(condition.start, event, state, condition.type, previousChanges)
                    && condition.wantedStatesAny.matchAny()
                    && condition.wantedStatesAll.matchAll()
                    && condition.unwantedStates.matchAll()

            STATE -> match(condition.start, event, state, condition.type, previousChanges)
                    && condition.wantedEventsAny.matchAny()
                    && condition.wantedEventsAll.matchAll()
                    && condition.unwantedEvents.matchAll()
        } logV {
            @Suppress("RemoveCurlyBracesFromTemplate")
            m = "$it => $condition <==> ${when (condition.type) {
                EVENT -> state
                STATE -> event
            }}"
        }
    }

    private fun match(
        primary: IMaster,
        event: IEvent,
        state: IState,
        type: ConditionType,
        previousChanges: List<OnStateChanged>,
        secondary: IMaster = when (type) {
            EVENT -> state
            STATE -> event
        }
    ): Boolean = when {
        primary.isRuntime && secondary.isDefinition -> matchInternal(
            primary,
            event,
            state,
            type,
            previousChanges,
            secondary
        )
        primary.isDefinition && secondary.isRuntime -> matchInternal(
            secondary,
            event,
            state,
            type,
            previousChanges,
            primary
        )
        else -> throw IllegalArgumentException(
            "Invalid matching master=$primary; " +
                    "slave=$secondary; event=$event;  state=$state; combination: $type"
        )
    }

    @Suppress("RemoveCurlyBracesFromTemplate")
    private fun matchInternal(
        runtime: IMaster,
        event: IEvent,
        state: IState,
        type: ConditionType,
        previousChanges: List<OnStateChanged>,
        definition: IMaster
    ): Boolean {
        if (runtime.isDefinition) throw IllegalStateException("RUNTIME type must be first item in match.")

        return when (runtime) {
            is ISingle -> when (definition) {
                is IEvent,
                is IState -> runtime == definition
                is IData -> runtime == definition
                is IGroup,
                is IType<*> -> matchClass(definition)
                else -> throw IllegalArgumentException("Unknown type to match. first=$runtime; second=$definition")
            }
            is IGroup -> matchClass(definition)
            else -> throw IllegalArgumentException("Unknown type to match. first=$this; second=$this")
        } logV { m = "$it => $runtime <==> $definition" }
    }
}

internal fun Any?.matchClass(other: Any?) = this != null && other != null && run {
    val oClazz = other::class

    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    val tClazz = this!!::class

    oClazz.isInstance(this)
            || tClazz.isInstance(other)
            || oClazz.isSubclassOf(tClazz)
            || oClazz.isSuperclassOf(tClazz)
}