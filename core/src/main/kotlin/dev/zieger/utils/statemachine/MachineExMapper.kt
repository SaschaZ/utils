@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.Matcher.IMatchScope
import dev.zieger.utils.statemachine.Matcher.MatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition
import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.ICondition

/**
 * Responsible to map the incoming [IEvent]s to their [IState]s defined by provided mappings.
 */
interface IMachineExMapper {

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

    fun bind(condition: ICondition, machine: IMachineEx) {
        bindings[condition] = machine
    }

    var lastId: Long

    private val newId: Long get() = ++lastId

    suspend fun findStateForEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>
    ): IComboElement? = Matcher.run {
        MatchScope(event, state, previousChanges, conditions, bindings).findStateForEvent()
    }
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, ICondition> = HashMap()
    override val bindings: MutableMap<ICondition, IMachineEx> = HashMap()
    override var lastId: Long = -1L
}