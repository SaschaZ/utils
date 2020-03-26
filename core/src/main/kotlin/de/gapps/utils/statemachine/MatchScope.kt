package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE
import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.IState

data class MatchScope(
    val event: IEvent,
    val stateBefore: IState,
    val stateAfter: IState? = null,
    val conditionType: ConditionType,
    val previousChanges: List<OnStateChanged>
) {
    val state: IState
        get() = when (conditionType) {
            EVENT -> stateBefore
            STATE -> stateAfter
                ?: throw IllegalStateException("stateAfter is Null when matching for state condition.")
        }
}