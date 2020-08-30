@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.STATE
import java.util.concurrent.atomic.AtomicLong

/**
 * Responsible to map the incoming [IEvent]s to their [IState]s defined by provided mappings.
 */
interface IMachineExMapper {

    companion object {
        private var lastId = AtomicLong(0L)
        private val newId: Long get() = lastId.getAndIncrement()
    }

    val conditions: MutableMap<Long, ICondition>
    val bindings: MutableMap<ICondition, IMachineEx>

    /**
     *
     */
    fun addCondition(
        condition: Condition,
        action: suspend IMatchScope.() -> IComboElement?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition", logFilter = GENERIC(disableLog = MachineEx.debugLevel <= INFO))
        conditions[id] = condition.copy(action = action)
    }

    fun addBinding(condition: ICondition, machine: IMachineEx) {
        bindings[condition] = machine
    }

    /**
     * Is called to determine the next state when a new event is processed.
     * Also executes possible event and state actions.
     *
     * @return new state
     */
    suspend fun processEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>
    ): Pair<IComboElement, suspend () -> Unit>? =
        MatchScope(event, state, previousChanges, conditions, bindings).log.run {
            (stateForEventBinding() ?: stateForEvent())?.let { newState ->
                newState to suspend {
                    applyState(newState).run innerRun@{
                        matchingStateConditions().forEach { it.action?.invoke(this@innerRun) }
                    }
                }
            }
        }

    private val IMatchScope.log: IMatchScope
        get() = apply {
            Log.v(
                "New incoming event $newEvent with state $currentState.",
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
            )
        }

    private suspend fun IMatchScope.stateForEventBinding(): IComboElement? =
        bindings.filter { match(it.key, EVENT) }.values.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching event binding for $this.")
            }
        }?.setEvent(newEvent)

    private suspend fun IMatchScope.bindingForState(): IMachineEx? =
        bindings.filter { match(it.key, STATE) }.values.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching state binding for $this.")
            }
        }

    private suspend fun IMatchScope.stateForEvent(): IComboElement? =
        matchingEventConditions().mapNotNull { it.action?.invoke(this) }.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching event condition for $this.")
            }
        }?.log(newEvent)

    private fun IComboElement.log(event: IComboElement): IComboElement = this.apply {
        Log.i(
            "Found new state $this for event $event.",
            logFilter = GENERIC(disableLog = noLogging || MachineEx.debugLevel <= ERROR)
        )
    }

    private suspend fun IMatchScope.matchingEventConditions(): Collection<ICondition> =
        conditions.filter { match(it.value, EVENT) }.values

    private suspend fun IMatchScope.matchingStateConditions(): Collection<ICondition> =
        conditions.filter { match(it.value, STATE) }.values

    private suspend fun IMatchScope.match(
        condition: ICondition,
        type: ICondition.ConditionType
    ) = (condition.type == type && condition.run { match(InputElement(newEvent, currentState)) }) logV
            {
                f = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
                m = "#R $it => ${type.name[0]} $condition <||> $newEvent, $currentState"
            }
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, ICondition> = HashMap()
    override val bindings: MutableMap<ICondition, IMachineEx> = HashMap()
}