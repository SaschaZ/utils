package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*

/**
 * TODO
 */
interface IMachineEx {

    val event: Event
    val eventData: Data?

    suspend fun setEventSync(event: Event): StateCombo? = setEventSync(event.combo)
    suspend fun setEventSync(event: EventCombo): StateCombo?

    val state: State
    val stateData: Data?

    suspend fun suspendUtilProcessingFinished()

    fun fireAndForget(event: Event) = fireAndForget(event.combo)
    fun fireAndForget(event: EventCombo)

    fun clearPreviousChanges()

    fun release()
}

