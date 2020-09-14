@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.Event
import dev.zieger.utils.statemachine.conditionelements.EventCombo
import dev.zieger.utils.statemachine.conditionelements.State
import dev.zieger.utils.statemachine.conditionelements.combo

val IMachineEx.fire get() = FireScope(this)

class FireScope(private val machine: IMachineEx) {

    infix fun event(event: Event) = event(event.combo)
    infix fun event(event: EventCombo) = machine.fireAndForget(event)

    suspend infix fun eventSync(event: Event): State? = eventSync(event.combo)
    suspend infix fun eventSync(event: EventCombo): State? = machine.setEventSync(event)
}