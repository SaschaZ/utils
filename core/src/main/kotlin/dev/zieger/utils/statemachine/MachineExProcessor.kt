@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log2.ILogScope
import dev.zieger.utils.log2.filter.LogCondition
import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE

/**
 * Responsible to map the incoming [AbsEvent]s to their [AbsState]s defined by provided mappings.
 */
interface IMachineExProcessor : ILogScope {

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
     *
     * 1. check matching bindings -> fire event to binding -> result is new state
     * 2. otherwise check matching event conditions -> execute actions -> result can be one new state only
     *
     * @return new state or `null` if the state did not changed
     */
    suspend fun processEvent(
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>
    ): StateCombo? = MatchScope(event, state, previousChanges, conditions, bindings).log.run {
        (matchingEventBinding()?.run { fire(event) } ifNull {
            matchingEventConditions()
                .mapNotNull { it.action?.invoke(this) }.run {
                    filterIsInstance<AbsState>().run {
                        when (size) {
                            in 0..1 -> firstOrNull()
                            else -> throw IllegalStateException("More than one matching event condition setting a state for $this.")
                        }
                    }
                }
        })?.combo?.logNewState(event)
    }

    /**
     * Is called when a new state was set.
     * Will execute all actions of the matching state conditions.
     *
     * @return new event to fire or `null`
     */
    suspend fun processState(
        event: EventCombo,
        state: StateCombo,
        previousChanges: List<OnStateChanged>
    ): EventCombo? = MatchScope(event, state, previousChanges, conditions, bindings).run {
        matchingStateConditions().mapNotNull { it.action?.invoke(this) }.run {
            filterIsInstance<AbsEvent>().run {
                when (size) {
                    in 0..1 -> firstOrNull()?.combo
                    else -> throw IllegalStateException("More than one matching state condition setting a state for $this.")
                }
            }
        }
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
    ) = Matcher(this, this@IMachineExProcessor).run {
        (condition.type == type && condition.match()) logD {
            filter = LogCondition { !event.noLogging }
            "#R $it => ${type.name[0]} $condition <||> (E: $event | S: $state)"
        }
    }

    private val MatchScope.log: MatchScope
        get() = apply {
            Log.i("NEW INCOMING EVENT $eventCombo with state $stateCombo.", filter = LogCondition { !noLogging })
        }

    private fun StateCombo.logNewState(event: AbsEvent): StateCombo =
        apply { Log.i("SETTING NEW STATE $this for event $event.", filter = LogCondition { !noLogging }) }
}

internal class MachineExProcessor(logScope: ILogScope) : IMachineExProcessor, ILogScope by logScope {

    override val conditions: MutableList<Condition> = ArrayList()
    override val bindings: MutableMap<Condition, IMachineEx> = HashMap()
}