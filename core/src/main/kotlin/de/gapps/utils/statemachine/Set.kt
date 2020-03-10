@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.BaseType.ValueDataHolder

val IMachineEx.set get() = SetScope(this)

class SetScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: ValueDataHolder) {
        this.event = event
    }

    suspend infix fun eventSync(event: ValueDataHolder) {
        event(event)
        suspendUtilProcessingFinished()
    }
}