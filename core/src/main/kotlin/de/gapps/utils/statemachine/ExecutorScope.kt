package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Master.State
import de.gapps.utils.statemachine.ConditionElement.Slave.Data
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement
import de.gapps.utils.statemachine.IConditionElement.ISlave

data class ExecutorScope(
    val eventHolder: ICombinedConditionElement,
    val stateHolder: ICombinedConditionElement,
    val previousChanges: Set<OnStateChanged>
) {
    val events = previousChanges.map { it.event }
    val statesBefore = previousChanges.map { it.stateBefore }
    val statesAfter = previousChanges.map { it.stateAfter }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = eventHolder.master as T

    @Suppress("UNCHECKED_CAST")
    fun <T : State> state(): T = stateHolder.master as T

    val eventData: Set<ISlave> = eventHolder.slaves
    val stateData: Set<ISlave> = stateHolder.slaves

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> eventData(idx: Int = 0) =
        eventData.filterIsInstance<D>()[idx]

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> stateData(idx: Int = 0) =
        stateData.filterIsInstance<D>()[idx]
}