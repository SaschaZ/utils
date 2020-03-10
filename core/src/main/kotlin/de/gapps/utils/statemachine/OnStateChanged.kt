package de.gapps.utils.statemachine

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.BaseType.ValueDataHolder

data class OnStateChanged(
    val event: ValueDataHolder,
    val stateBefore: ValueDataHolder,
    val stateAfter: ValueDataHolder
) {
    override fun toString(): String =
        "${this::class.name}(event=$event, stateBefore=$stateBefore, stateAfter=$stateAfter)"
}

fun Collection<Any>.joinToStringTabbed(tabCount: Int = 1): String {
    fun newLineTapped(tabCount: Int): String {
        return if (tabCount == 0) "" else "\n${(0..tabCount).joinToString("") { "\t" }}"
    }
    return "${newLineTapped(tabCount)}${joinToString(newLineTapped(tabCount))}"
}