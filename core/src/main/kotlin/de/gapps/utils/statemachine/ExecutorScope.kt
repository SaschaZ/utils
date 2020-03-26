package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData

data class ExecutorScope(
    val event: IEvent,
    val state: IState,
    val previousChanges: List<OnStateChanged>
) {
    val events = previousChanges.map { it.event }
    val statesBefore = previousChanges.map { it.stateBefore }
    val statesAfter = previousChanges.map { it.stateAfter }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : IData> eventData() = eventData as D

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : IData> stateData(idx: Int = 0) = stateData as D

    val eventData: ISlave? = event.slave
    val stateData: ISlave? = state.slave
}