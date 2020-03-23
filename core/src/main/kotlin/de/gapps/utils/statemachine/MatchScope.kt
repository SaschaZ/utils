package de.gapps.utils.statemachine

data class MatchScope(
    val event: IConditionElement.ICombinedConditionElement,
    val state: IConditionElement.ICombinedConditionElement,
    val previousChanges: Set<OnStateChanged>
)