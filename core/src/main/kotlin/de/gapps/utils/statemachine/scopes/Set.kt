package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx

val IMachineEx.set get() = SetScope(this)

class SetScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: IEvent) {
        this.event = event
    }

    suspend infix fun eventSync(event: IEvent) {
        event(event)
        suspendUtilProcessingFinished()
    }
}

infix fun IEvent.withData(data: IData?): IEvent {
    this.data = data
    return this
}