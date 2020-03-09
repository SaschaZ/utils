package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.Data
import de.gapps.utils.statemachine.ValueDataHolder

data class ExecutorScope(
    val event: ValueDataHolder,
    val state: ValueDataHolder
) {
    val eventData: Set<Data> = event.data
    val stateData: Set<Data> = state.data

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> eventData(idx: Int = 0) = eventData.filterIsInstance<D>()[idx] as? D

    @Suppress("UNCHECKED_CAST")
    inline fun <reified D : Data> stateData(idx: Int = 0) = stateData.filterIsInstance<D>()[idx] as? D
}