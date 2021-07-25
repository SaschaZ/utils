package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.EventCombo
import dev.zieger.utils.statemachine.conditionelements.StateCombo

data class OnStateChanged(
    val event: EventCombo,
    val stateBefore: StateCombo,
    val stateAfter: StateCombo
)