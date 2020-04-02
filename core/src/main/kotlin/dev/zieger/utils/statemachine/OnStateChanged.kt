package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.IConditionElement.IComboElement

data class OnStateChanged(
    val event: IComboElement,
    val stateBefore: IComboElement,
    val stateAfter: IComboElement
)

fun Collection<Any>.joinToStringTabbed(tabCount: Int = 1): String {
    fun newLineTapped(tabCount: Int): String {
        return if (tabCount == 0) "" else "\n${(0..tabCount).joinToString("") { "\t" }}"
    }
    return "${newLineTapped(tabCount)}${joinToString(newLineTapped(tabCount))}"
}