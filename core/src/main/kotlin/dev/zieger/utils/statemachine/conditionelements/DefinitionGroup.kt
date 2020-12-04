@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

data class DefinitionGroup(
    val matchType: MatchType,
    val elements: MutableList<Definition>
) : ConditionElement, MutableList<Definition> by elements {

    enum class MatchType {
        ALL,
        ANY,
        NONE
    }

    override fun toString(): String = "DG($matchType; $elements)"
}