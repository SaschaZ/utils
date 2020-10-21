@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.AbsEvent
import dev.zieger.utils.statemachine.conditionelements.EventCombo
import dev.zieger.utils.statemachine.conditionelements.StateCombo
import dev.zieger.utils.statemachine.conditionelements.combo

val IMachineEx.fire get() = FireScope(this)

class FireScope(private val machine: IMachineEx) {

    infix fun event(event: AbsEvent) = event(event.combo)
    infix fun event(event: EventCombo) = machine.fireAndForget(event)

    suspend infix fun eventSync(event: AbsEvent): StateCombo? = eventSync(event.combo)
    suspend infix fun eventSync(event: EventCombo): StateCombo? = machine.fire(event)
}