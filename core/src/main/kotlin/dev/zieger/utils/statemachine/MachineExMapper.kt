@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.statemachine.ConditionElement.Condition
import dev.zieger.utils.statemachine.IConditionElement.IComboElement
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.ERROR

/**
 * Responsible to map the incoming [IEvent]s to their [IState]s defined by provided mappings.
 */
interface IMachineExMapper {

    val conditions: MutableMap<Long, Condition>

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

    /**
     *
     */
    fun removeMapping(id: Long) = conditions.remove(id)

    suspend fun findStateForEvent(
        event: IComboElement,
        state: IComboElement,
        previousChanges: List<OnStateChanged>
    ): IComboElement? =
        Matcher.findStateForEvent(event, state, previousChanges, conditions)
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, Condition> = HashMap()
    override var lastId: Long = -1L
}