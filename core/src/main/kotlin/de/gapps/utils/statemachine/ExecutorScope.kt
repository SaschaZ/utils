package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData

data class ExecutorScope(
    val event: IEvent,
    val state: IState,
    val previousChanges: List<OnStateChanged>
) {

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> eventData() = eventData as D

    @Suppress("UNCHECKED_CAST")
    fun <D : IData> stateData(idx: Int = 0) = stateData as D

    val eventData: ISlave? = event.slave
    val stateData: ISlave? = state.slave
}