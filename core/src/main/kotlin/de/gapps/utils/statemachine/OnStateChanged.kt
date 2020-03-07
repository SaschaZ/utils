package de.gapps.utils.statemachine

import de.gapps.utils.misc.name

data class OnStateChanged(
    val event: ValueDataHolder,
    val stateBefore: ValueDataHolder,
    val stateAfter: ValueDataHolder,
    val recentChanges: Set<OnStateChanged>
) {
    override fun toString(): String =
        "${this::class.name}(event=$event, stateBefore=$stateBefore, stateAfter=$stateAfter, " +
                "recentChanges=${recentChanges.joinToStringTabbed(3)})"
}

fun Collection<Any>.joinToStringTabbed(tabCount: Int = 1): String {
    fun newLineTapped(tabCount: Int): String {
        return if (tabCount == 0) "" else "\n${(0..tabCount).joinToString("") { "\t" }}"
    }
    return "${newLineTapped(tabCount)}${joinToString(newLineTapped(tabCount))}"
}