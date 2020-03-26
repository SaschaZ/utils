package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.EVENT
import de.gapps.utils.statemachine.IConditionElement.ICondition.ConditionType.STATE

data class MatchScope(
    val event: IConditionElement.IMaster.ISingle.IEvent,
    val stateBefore: IConditionElement.IMaster.ISingle.IState,
    val stateAfter: IConditionElement.IMaster.ISingle.IState? = null,
    val conditionType: ConditionType,
    val previousChanges: List<OnStateChanged>
) {
    val state: IConditionElement.IMaster.ISingle.IState
        get() = when (conditionType) {
            EVENT -> stateBefore
            STATE -> stateAfter
                ?: throw IllegalStateException("stateAfter is Null when matching for state condition.")
        }
}