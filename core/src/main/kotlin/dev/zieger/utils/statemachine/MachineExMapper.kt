@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR
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
        action: suspend ExecutorScope.() -> IComboElement?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition", logFilter = GENERIC(disableLog = MachineEx.debugLevel == ERROR))
        conditions[id] = condition.copy(action = action)
    }

    var lastId: Long

    private val newId: Long
        get() = ++lastId

    suspend fun findStateForEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>
    ): IComboElement? =
        Matcher.findStateForEvent(event, state, previousChanges, conditions, bindings)

    fun bind(condition: ICondition, machine: IMachineEx) {
        bindings[condition] = machine
    }
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, ICondition> = HashMap()
    override val bindings: MutableMap<ICondition, IMachineEx> = HashMap()
    override var lastId: Long = -1L
}