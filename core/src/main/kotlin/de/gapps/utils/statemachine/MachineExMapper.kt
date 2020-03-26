@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.Log
import de.gapps.utils.statemachine.ConditionElement.Condition
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState

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
        action: suspend ExecutorScope.() -> IState?
    ): Long = newId.also { id ->
        Log.v("add condition: $id => $condition")
        conditions[id] = condition.copy(action = action)
    }

    var lastId: Long

    private val newId: Long
        get() = ++lastId

    /**
     *
     */
    fun removeMapping(id: Long) = conditions.remove(id)

    suspend fun findStateForEvent(event: IEvent, state: IState, previousChanges: List<OnStateChanged>): IState? =
        Matcher.findStateForEvent(event, state, previousChanges, conditions)
}

class MachineExMapper : IMachineExMapper {

    override val conditions: MutableMap<Long, Condition> = HashMap()
    override var lastId: Long = -1L
}