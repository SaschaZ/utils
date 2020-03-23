@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.CombinedConditionElement
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs.RUNTIME

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: ConditionElement) {
        when (event) {
            is Event -> this.event = CombinedConditionElement(event, usedAs = RUNTIME)
            is CombinedConditionElement -> {
                this.event = event.apply { usedAs = RUNTIME }
            }
            else -> throw IllegalArgumentException("Can not set type ${event::class} as an event.")
        }
    }

    suspend infix fun eventSync(event: ConditionElement) {
        event(event)
        suspendUtilProcessingFinished()
    }
}