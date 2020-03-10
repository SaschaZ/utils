package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.BaseType.*

data class ExecutorScope(
    val eventHolder: ValueDataHolder,
    val stateHolder: ValueDataHolder,
    val previousChanges: Set<OnStateChanged>
) {
    val events = previousChanges.map { it.event }
    val statesBefore = previousChanges.map { it.stateBefore }
    val statesAfter = previousChanges.map { it.stateAfter }

    @Suppress("UNCHECKED_CAST")
    fun <T : Event> event() = eventHolder.value as T

    @Suppress("UNCHECKED_CAST")
    fun <T : State> state(): T = stateHolder.value as T

    val eventData: Set<Data> = eventHolder.data
    val stateData: Set<Data> = stateHolder.data

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> eventData(idx: Int = 0) =
        eventData.filterIsInstance<D>()[idx]

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> stateData(idx: Int = 0) =
        stateData.filterIsInstance<D>()[idx]
}