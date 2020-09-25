@file:Suppress("UNUSED_PARAMETER")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.STATE

/**
 * Holds methods that are used for matching the incoming events to a new state and/or action and to match the new state to actions.
 */
object Matcher {

    /**
     *
     * @property newEvent new event
     * @property currentState current state
     * @property previousChanges previous state changes of the state machine
     * @property conditions
     * @property bindings
     */
    interface IMatchScope {
        val newEvent: IComboElement
        val currentState: IComboElement
        val previousChanges: List<OnStateChanged>
        val conditions: Map<Long, ICondition>
        val bindings: Map<ICondition, IMachineEx>

        val eventData: ISlave? get() = newEvent.slave
        val stateData: ISlave? get() = currentState.slave

        @Suppress("UNCHECKED_CAST")
        fun <D : IData> eventData() = eventData as D

        @Suppress("UNCHECKED_CAST")
        fun <D : IData> stateData(idx: Int = 0) = stateData as D

        fun applyState(state: IComboElement): IMatchScope
    }

    data class MatchScope(
        override val newEvent: IComboElement,
        override val currentState: IComboElement,
        override val previousChanges: List<OnStateChanged> = emptyList(),
        override val conditions: Map<Long, ICondition> = emptyMap(),
        override val bindings: Map<ICondition, IMachineEx> = emptyMap()
    ) : IMatchScope {

        override fun applyState(state: IComboElement): IMatchScope = copy(currentState = state)
    }

    /**
     * Is called to determine the next state when a new event is processed.
     * Also executes possible actions.
     *
     * @return new state
     */
    suspend fun IMatchScope.findStateForEvent(): IComboElement? {
        Log.v(
            "findStateForEvent()\n\tevent=$newEvent;\n\tstate=$currentState;\n\t" +
                    "previousChanges=${previousChanges.toList().take(3).joinToStringTabbed(2)}",
            logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
        )

        val matchingEventBindings = bindings.filter { match(it.key, EVENT) }
        return when (matchingEventBindings.size) {
            1 -> matchingEventBindings.values.first().setEvent(newEvent).executePossibleStateConditions(this)
            0 -> processEvent()
            else -> throw IllegalStateException("More than one binding found for condition.")
        }
    }

    private suspend fun IMatchScope.processEvent(): IComboElement? {
        val matchingEventConditions = conditions.filter { match(it.value, EVENT) }
        if (matchingEventConditions.isEmpty()) {
            Log.d(
                "No event condition matches for $newEvent and $currentState.",
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
            )
            return null
        }

        val matchedResults = matchingEventConditions.mapNotNull {
            it.value.action?.invoke(this)
        }

        val newState = when (matchedResults.size) {
            in 0..1 -> matchedResults.firstOrNull()
            else -> throw IllegalStateException(
                "To much states defined for $newEvent and $currentState " +
                        "with mappedEvents=${matchedResults.joinToStringTabbed()}"
            )
        }

        Log.v(
            "\n\tnewState=$newState" +
                    "\n\tmatchingEventConditions=${matchingEventConditions.toList().joinToStringTabbed(2)}",
            logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
        )

        return newState.executePossibleStateConditions(this) ifNull {
            Log.i(
                "No event condition matches for $newEvent and $currentState. Had ${matchingEventConditions.size}" +
                        " matches:${matchingEventConditions.values.joinToStringTabbed(2)}",
                logFilter = GENERIC(
                    disableLog =
                    matchingEventConditions.values.isNotEmpty() || newEvent.noLogging
                            || MachineEx.debugLevel <= INFO
                )
            )
            null
        }
    }

    private suspend fun IComboElement?.executePossibleStateConditions(
        scope: IMatchScope
    ): IComboElement? = this?.apply {
        val newState = this
        val prevState = scope.currentState
        scope.applyState(newState).run {
            bindings.filter { match(it.key, STATE) }.forEach { it.key.action?.invoke(this) }

            Log.v(
                "searching matching state conditions:",
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
            )
            val matchingStateConditions = conditions.filter {
                match(it.value, STATE)
            }

            Log.v(
                "executing matching state conditions:" +
                        matchingStateConditions.entries.joinToStringTabbed(2),
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
            )

            matchingStateConditions.forEach { it.value.action?.invoke(scope) }

            Log.i(
                "state changed from $prevState to $newState with event $newEvent",
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= ERROR)
            )
        }
    }

    private suspend fun IMatchScope.match(
        condition: ICondition,
        type: ICondition.ConditionType
    ) = (condition.type == type && condition.run { match(InputElement(newEvent, currentState)) }) logV
            {
                f = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
                m = "#R $it => ${type.name[0]} $condition <||> $newEvent, $currentState"
            }
}