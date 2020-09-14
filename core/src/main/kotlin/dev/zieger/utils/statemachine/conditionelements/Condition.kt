@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*
import dev.zieger.utils.statemachine.conditionelements.DefinitionElementGroup.MatchType.*

data class Condition(
    val start: Combo<*>,
    val items: List<DefinitionElementGroup> = INITIAL_ITEMS.apply { first { it.matchType == ALL }.add(start) },
    val action: (suspend IMatchScope.() -> State?)? = null
) : ConditionElement {

    constructor(start: Master) : this(
        when (start) {
            is Combo<*> -> start
            is Event -> start.combo
            is State -> start.combo
            is EventGroup<*> -> start.combo
            is StateGroup<*> -> start.combo
            else -> throw IllegalArgumentException("Unknown Master type ${start::class.name}")
        }, action = null
    )

    companion object {
        private val INITIAL_ITEMS
            get() = listOf(
                DefinitionElementGroup(ANY, ArrayList()),
                DefinitionElementGroup(ALL, ArrayList()),
                DefinitionElementGroup(NONE, ArrayList())
            )
    }

    val any: DefinitionElementGroup get() = items.first { it.matchType == ANY }
    val all: DefinitionElementGroup get() = items.first { it.matchType == ALL }
    val none: DefinitionElementGroup get() = items.first { it.matchType == NONE }

    enum class DefinitionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: DefinitionType
        get() = start.master.type

    private fun Master.getType(): DefinitionType = when (this) {
        is Event,
        is EventGroup<*> -> EVENT
        is State,
        is StateGroup<*> -> STATE
        is External -> EXTERNAL
        is Combo<*> -> when (master) {
            is Event,
            is EventGroup<*> -> EVENT
            is State,
            is StateGroup<*> -> STATE
            is External -> EXTERNAL
            else -> throw IllegalArgumentException("Unknown Master of type $this")
        }
        else -> throw IllegalArgumentException("Unknown Master of type $this")
    }

    override fun toString(): String = "C($items)"
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT