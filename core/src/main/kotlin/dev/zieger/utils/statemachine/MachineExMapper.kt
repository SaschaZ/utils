@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log2.ILogScope
import dev.zieger.utils.log2.filter.LogCondition
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE

/**
 * Responsible to map the incoming [AbsEvent]s to their [AbsState]s defined by provided mappings.
 */
interface IMachineExMapper : ILogScope {

    val conditions: MutableList<Condition>
    val bindings: MutableMap<Condition, IMachineEx>

    fun addCondition(
        condition: Condition,
        action: suspend IMatchScope.() -> Master?
    ) {
        Log.d("add condition: $condition")
        conditions.add(condition.copy(action = action))
    }

    fun addBinding(condition: Condition, machine: IMachineEx) {
        Log.d("add binding: $condition to $machine")
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
        previousChanges: List<OnStateChanged>,
        onNewEvent: (event: AbsEvent) -> Unit
    ): StateCombo? = MatchScope(event, state, previousChanges, conditions, bindings).log.run {
        val boundMachine = matchingEventBinding()
        (if (boundMachine == null)
            matchingEventConditions()
                .mapNotNull { it.action?.invoke(this) }.run {
                    filterIsInstance<AbsEvent>().forEach { onNewEvent(it) }
                    filterIsInstance<AbsState>().run {
                        when (size) {
                            in 0..1 -> firstOrNull()
                            else -> throw IllegalStateException("More than one matching event condition setting a state for $this.")
                        }
                    }
                }
        else boundMachine.fire(event))?.combo?.logNewState(event)
    }

    suspend fun processState(
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>,
        onNewEvent: (event: AbsEvent) -> Unit
    ): StateCombo? = MatchScope(event, state, previousChanges, conditions, bindings).run {
        matchingStateConditions().mapNotNull { it.action?.invoke(this) }.run {
            filterIsInstance<AbsEvent>().forEach { onNewEvent(it) }
            filterIsInstance<AbsState>().run {
                when (size) {
                    in 0..1 -> firstOrNull()?.combo
                    else -> throw IllegalStateException("More than one matching state condition setting a state for $this.")
                }
            }
        }?.logNewState(event)
    }

    private suspend fun IMatchScope.matchingEventBinding(): IMachineEx? =
        bindings.filter { match(it.key, EVENT) }.values.firstOrNull()

    private suspend fun IMatchScope.matchingEventConditions(): Collection<Condition> =
        conditions.filter { match(it, EVENT) }

    private suspend fun IMatchScope.matchingStateConditions(): Collection<Condition> =
        conditions.filter { match(it, STATE) }

    private suspend fun IMatchScope.match(
        condition: Condition,
        type: Condition.DefinitionType
    ) = Matcher(this, this@IMachineExMapper).run {
        (condition.type == type && condition.match()) logV {
            filter = LogCondition { !event.noLogging }
            "#R $it => ${type.name[0]} $condition <||> $event, $state"
        }
    }

    private val MatchScope.log: MatchScope
        get() = apply {
            Log.i("New incoming event $eventCombo with state $stateCombo.", filter = LogCondition { !noLogging })
        }

    private fun StateCombo.logNewState(event: AbsEvent): StateCombo =
        apply { Log.i("Setting new state $this for event $event.", filter = LogCondition { !noLogging }) }
}

internal class MachineExMapper(logScope: ILogScope) : IMachineExMapper, ILogScope by logScope {

    override val conditions: MutableList<Condition> = ArrayList()
    override val bindings: MutableMap<Condition, IMachineEx> = HashMap()
}