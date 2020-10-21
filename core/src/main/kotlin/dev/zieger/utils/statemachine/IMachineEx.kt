package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*
import kotlinx.coroutines.Job

/**
 * Base interface for a state machine. Has values for accessing current state, last event and their possible data.
 * Setting a new event is possible with the suspend methods [fire] and the non suspend method [fireAndForget].
 */
interface IMachineEx {

    val event: AbsEvent
    val eventData: Data?

    val state: AbsState
    val stateData: Data?

    suspend fun fire(event: AbsEvent): StateCombo? = fire(event.combo)
    suspend fun fire(event: EventCombo): StateCombo?

    fun fireAndForget(event: AbsEvent): Job = fireAndForget(event.combo)
    fun fireAndForget(event: EventCombo): Job

    fun clearPreviousChanges()

    fun release()
}

