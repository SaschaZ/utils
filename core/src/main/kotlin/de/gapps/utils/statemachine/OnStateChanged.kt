package de.gapps.utils.statemachine

data class OnStateChanged(
    val event: ValueDataHolder,
    val stateBefore: ValueDataHolder,
    val stateAfter: ValueDataHolder,
    val recentChanges: Set<OnStateChanged>
)