@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.CombinedConditionElement
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement.UsedAs.RUNTIME
import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: IConditionElement) {
        when (event) {
            is IEvent -> this.event = CombinedConditionElement(event, usedAs = RUNTIME)
            is ICombinedConditionElement -> {
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