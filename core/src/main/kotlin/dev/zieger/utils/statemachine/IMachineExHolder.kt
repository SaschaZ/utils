@file:Suppress("unused")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.AbsEvent
import dev.zieger.utils.statemachine.conditionelements.AbsState
import dev.zieger.utils.statemachine.conditionelements.Data
import dev.zieger.utils.statemachine.conditionelements.EventCombo

interface IMachineExHolder : IMachineEx {

    val machine: IMachineEx

    override val event: AbsEvent get() = machine.event
    override val eventData: Data? get() = machine.eventData
    override val state: AbsState get() = machine.state
    override val stateData: Data? get() = machine.stateData

    override suspend fun fire(event: EventCombo) = machine.fire(event)
    override fun fireAndForget(event: EventCombo) = machine.fireAndForget(event)

    override fun clearPreviousChanges() = machine.clearPreviousChanges()

    override fun release() = machine.release()
}