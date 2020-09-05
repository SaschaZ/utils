@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.conditionelements.ComboEventElement
import dev.zieger.utils.statemachine.conditionelements.ComboStateElement
import dev.zieger.utils.statemachine.conditionelements.Event
import dev.zieger.utils.statemachine.conditionelements.comboEvent

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: Event) = event(event.comboEvent)
    infix fun event(event: ComboEventElement) = scope.launchEx { setEvent(event) }.asUnit()

    suspend infix fun eventSync(event: Event): ComboStateElement= eventSync(event.comboEvent)
    suspend infix fun eventSync(event: ComboEventElement): ComboStateElement= setEvent(event)
}