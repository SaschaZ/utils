package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.ComboEventElement
import dev.zieger.utils.statemachine.conditionelements.ComboStateElement

data class OnStateChanged(
    val event: ComboEventElement,
    val stateBefore: ComboStateElement,
    val stateAfter: ComboStateElement
)