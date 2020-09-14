@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

data class DefinitionElementGroup(
    val matchType: MatchType,
    val elements: MutableList<DefinitionElement>
) : ConditionElement, MutableList<DefinitionElement> by elements {

    enum class MatchType {
        ALL,
        ANY,
        NONE
    }

    override fun toString(): String = "DG($matchType; $elements)"
}