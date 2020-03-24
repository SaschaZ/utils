package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement

data class MatchScope(
    val event: ICombinedConditionElement,
    val state: ICombinedConditionElement,
    val previousChanges: Set<OnStateChanged>
)