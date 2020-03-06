@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.Event
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.ValueDataHolder

val IMachineEx.set get() = SetScope(this)

class SetScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: ValueDataHolder<Event>) {
        this.event = event
    }

    suspend infix fun eventSync(event: ValueDataHolder<Event>) {
        event(event)
        suspendUtilProcessingFinished()
    }

    infix fun event(event: Event) {
        this.event = ValueDataHolder(event)
    }

    suspend infix fun eventSync(event: Event) {
        event(event)
        suspendUtilProcessingFinished()
    }
}