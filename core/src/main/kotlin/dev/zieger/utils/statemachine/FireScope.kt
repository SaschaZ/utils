@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.IEvent
import dev.zieger.utils.statemachine.conditionelements.combo

val IMachineEx.fire get() = FireScope(this)

class FireScope(machine: IMachineEx) : IMachineEx by machine {

    infix fun event(event: IEvent) = event(event.combo)
    infix fun event(event: IComboElement) = scope.launchEx { setEvent(event) }.asUnit()

    suspend infix fun eventSync(event: IEvent): IComboElement = eventSync(event.combo)
    suspend infix fun eventSync(event: IComboElement): IComboElement = setEvent(event)
}