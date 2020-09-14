@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

data class InputElement(
    val event: EventCombo,
    val state: StateCombo
) : ConditionElement {

    override fun toString(): String = "IE($event, $state)"
}