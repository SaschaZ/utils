@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.IEvent
import dev.zieger.utils.statemachine.conditionelements.combo

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: IEvent) = event(event.combo)
    infix fun event(event: IComboElement) {
        this.event = event
    }

    suspend infix fun eventSync(event: IEvent) = eventSync(event.combo)
    suspend infix fun eventSync(event: IComboElement) {
        event(event)
        suspendUtilProcessingFinished()
    }
}