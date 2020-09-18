package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*

/**
 * Base interface for a state machine. Has values for accessing current state, last event and their possible data.
 * Setting a new event is possible with the suspend methods [setEventSync] and the non suspend method [setEvent].
 */
interface IMachineEx {

    val event: Event
    val eventData: Data?

    val state: State
    val stateData: Data?

    suspend fun setEventSync(event: Event): StateCombo? = setEventSync(event.combo)
    suspend fun setEventSync(event: EventCombo): StateCombo?

    fun setEvent(event: Event) = setEvent(event.combo)
    fun setEvent(event: EventCombo)

    fun clearPreviousChanges()

    fun release()
}

