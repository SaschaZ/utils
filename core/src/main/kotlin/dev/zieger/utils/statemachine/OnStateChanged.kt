package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.IComboElement

data class OnStateChanged(
    val event: IComboElement,
    val stateBefore: IComboElement,
    val stateAfter: IComboElement
)