@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log2.ILogScope
import dev.zieger.utils.log2.filter.LogCondition
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE
import java.util.concurrent.atomic.AtomicLong

/**
 * Responsible to map the incoming [AbsEvent]s to their [AbsState]s defined by provided mappings.
 */
interface IMachineExMapper : ILogScope {

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
        action: suspend IMatchScope.() -> Master?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition", filter = LogCondition { MachineEx.debugLevel == DEBUG })
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
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>
    ): StateCombo? = MatchScope(event, state, previousChanges, conditions, bindings).log.run {
        comboForEventBinding() ?: finalCombo()
    }

    private val MatchScope.log: MatchScope
        get() = apply {
            Log.v(
                "\n\n\n\nNew incoming event $eventCombo with state $stateCombo.",
                filter = LogCondition { !noLogging && MachineEx.debugLevel < ERROR })
        }

    private suspend fun MatchScope.comboForEventBinding(): StateCombo? =
        bindings.filter { match(it.key, EVENT) }.values.let {
            when (it.size) {
                in 0..1 -> it.firstOrNull()
                else -> throw IllegalStateException("More than one matching event binding for $this.")
            }
        }?.fire(eventCombo)?.combo

    private suspend fun MatchScope.finalCombo(): StateCombo? =
        matchingEventConditions().mapNotNull { it.action?.invoke(this) }.let {
            when (it.filterIsInstance<AbsState>().size) {
                in 0..1 -> it.firstOrNull()?.combo as? StateCombo
                else -> throw IllegalStateException("More than one matching event condition setting a state for $this.")
            }
        }?.logNewState(eventCombo)

    private fun StateCombo.logNewState(event: AbsEvent): StateCombo =
        apply {
            Log.i(
                "Setting new state $this for event $event.\n\n",
                filter = LogCondition { !noLogging && MachineEx.debugLevel < ERROR })
        }

    private suspend fun IMatchScope.matchingEventConditions(): Collection<Condition> =
        conditions.filter { it.value.type != STATE && match(it.value, EVENT) }.values

    private suspend fun IMatchScope.matchingStateConditions(): Collection<Condition> =
        conditions.filter { it.value.type != EVENT && match(it.value, STATE) }.values

    private suspend fun IMatchScope.match(
        condition: Condition,
        type: Condition.DefinitionType
    ) = Matcher(this, this@IMachineExMapper).run {
        (condition.type == type && condition.match()) logV {
            filter = LogCondition { !event.noLogging && MachineEx.debugLevel == DEBUG }
            "#R $it => ${type.name[0]} $condition <||> $event, $state"
        }
    }

    suspend fun processState(
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>
    ): EventCombo? = MatchScope(event, state, previousChanges, conditions, bindings).run {
        matchingStateConditions().mapNotNull { it.action?.invoke(this) }.let {
            when (it.filterIsInstance<AbsEvent>().size) {
                in 0..1 -> it.firstOrNull()?.combo as? EventCombo
                else -> throw IllegalStateException("More than one matching event condition firing an event for $this.")
            }
        }?.logNewEvent(state)
    }

    private fun EventCombo.logNewEvent(state: AbsState): EventCombo =
        apply {
            Log.i(
                "Firing new event $this for state $state.\n\n",
                filter = LogCondition { !noLogging && MachineEx.debugLevel < ERROR })
        }
}

internal class MachineExMapper(logScope: ILogScope) : IMachineExMapper, ILogScope by logScope {

    override val conditions: MutableMap<Long, Condition> = HashMap()
    override val bindings: MutableMap<Condition, IMachineEx> = HashMap()
}