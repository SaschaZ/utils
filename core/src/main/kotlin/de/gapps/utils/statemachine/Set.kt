@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.BaseType.Primary.Event
import de.gapps.utils.statemachine.BaseType.ValueDataHolder

val IMachineEx.fire get() = SetScope(this)

class SetScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: BaseType) {
        when (event) {
            is Event -> this.event = ValueDataHolder(event)
            is ValueDataHolder -> this.event = event
            else -> throw IllegalArgumentException("Can not set type ${event::class} as an event.")
        }
    }

    suspend infix fun eventSync(event: BaseType) {
        event(event)
        suspendUtilProcessingFinished()
    }
}