package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.*

data class ExecutorScope(
    val event: ValueDataHolder<Event>,
    val state: ValueDataHolder<State>
) {
    val eventData: Any? = event.data
    val stateData: Any? = state.data

    @Suppress("UNCHECKED_CAST")
    fun <D : Any> eventData() = eventData as? D

    @Suppress("UNCHECKED_CAST")
    fun <D : Any> stateData() = stateData as? D
}