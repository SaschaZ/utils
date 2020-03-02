package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IData
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState

val IMachineEx<IData, IEvent, IState>.set get() = SetScope(this)

class SetScope<D: IData, E: IEvent, S: IState> (machine: IMachineEx<D, E, S>) : IMachineEx<D, E, S> by machine {

    infix fun event(event: E) {
        this.event = event
    }

    suspend infix fun eventSync(event: E) {
        this.event = event
        suspendUtilProcessingFinished()
    }

    // TODO withData
}
