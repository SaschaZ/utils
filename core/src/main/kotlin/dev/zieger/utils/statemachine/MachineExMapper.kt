@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE
import java.util.concurrent.atomic.AtomicLong

/**
 * Responsible to map the incoming [Event]s to their [State]s defined by provided mappings.
 */
interface IMachineExMapper {

    companion object {
        private var lastId = AtomicLong(0L)
        private val newId: Long get() = lastId.getAndIncrement()
    }

    val conditions: MutableMap<Long, Condition>
    val bindings: MutableMap<Condition, IMachineEx>

    /**
     *
     */
    fun addCondition(
        condition: Condition,
        action: suspend MatchScope.() -> ComboStateElement?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition", logFilter = GENERIC(disableLog = MachineEx.debugLevel <= INFO))
        conditions[id] = condition.copy(action = action)
    }

    fun addBinding(condition: Condition, machine: IMachineEx) {
        bindings[condition] = machine
    }

    /**
     * Is called to determine the next state when a new event is processed.
     * Also executes possible event and state actions.
     *
     * @return new state
     */
    suspend fun processEvent(
        event: ComboEventElement,
        state: ComboStateElement,
        previousChanges: List<OnStateChanged>
    ): Pair<ComboStateElement, suspend () -> Unit>? =
        MatchScope(event, state, previousChanges, conditions, bindings).log.run {
            (stateForEventBinding() ?: stateForEvent())?.let { newState ->
                newState to suspend {
                    applyState(newState).run innerRun@{
                        matchingStateConditions().forEach { it.action?.invoke(this@innerRun) }
                    }
                }
            }
        }

    private val MatchScope.log: MatchScope
        get() = apply {
            Log.v(
                "New incoming event $newEvent with state $currentState.",
                logFilter = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
            )
        }

    private suspend fun MatchScope.stateForEventBinding(): ComboStateElement? =
        bindings.filter { match(it.key, EVENT) }.values.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching event binding for $this.")
            }
        }?.setEvent(newEvent)

    private suspend fun MatchScope.bindingForState(): IMachineEx? =
        bindings.filter { match(it.key, STATE) }.values.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching state binding for $this.")
            }
        }

    private suspend fun MatchScope.stateForEvent(): ComboStateElement? =
        matchingEventConditions().mapNotNull { it.action?.invoke(this) }.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching event condition for $this.")
            }
        }?.log(newEvent)

    private fun ComboStateElement.log(event: ComboElement): ComboStateElement = this.apply {
        Log.i(
            "Found new state $this for event $event.",
            logFilter = GENERIC(disableLog = noLogging || MachineEx.debugLevel <= ERROR)
        )
    }

    private suspend fun MatchScope.matchingEventConditions(): Collection<Condition> =
        conditions.filter { match(it.value, EVENT) }.values

    private suspend fun MatchScope.matchingStateConditions(): Collection<Condition> =
        conditions.filter { match(it.value, STATE) }.values

    private suspend fun MatchScope.match(
        condition: Condition,
        type: Condition.DefinitionType
    ) = (condition.type == type && condition.run { match(InputElement(newEvent, currentState)) }) logV
            {
                f = GENERIC(disableLog = newEvent.noLogging || MachineEx.debugLevel <= INFO)
                m = "#R $it => ${type.name[0]} $condition <||> $newEvent, $currentState"
            }
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, Condition> = HashMap()
    override val bindings: MutableMap<Condition, IMachineEx> = HashMap()
}