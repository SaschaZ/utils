package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*
import kotlinx.coroutines.Job

/**
 * Base interface for a state machine. Has values for accessing current state, last event and their possible data.
 * Setting a new event is possible with the suspend methods [setEventSync] and the non suspend method [setEvent].
 */
interface IMachineEx {

    val event: AbsEvent
    val eventData: Data?

    val state: AbsState
    val stateData: Data?

    suspend fun setEventSync(event: AbsEvent): StateCombo? = setEventSync(event.combo)
    suspend fun setEventSync(event: EventCombo): StateCombo?

    fun setEvent(event: AbsEvent): Job = setEvent(event.combo)
    fun setEvent(event: EventCombo): Job

    fun clearPreviousChanges()

    fun release()
}

