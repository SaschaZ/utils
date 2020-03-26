@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.UsedAs.RUNTIME

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: IEvent) {
        event.usedAs = RUNTIME
        this.event = event
    }

    suspend infix fun eventSync(event: IEvent) {
        event(event)
        suspendUtilProcessingFinished()
    }
}