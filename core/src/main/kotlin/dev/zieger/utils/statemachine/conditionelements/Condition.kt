@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*
import dev.zieger.utils.statemachine.conditionelements.DefinitionElementGroup.MatchType.*

data class Condition(
    val start: Combo<*>,
    val items: List<DefinitionElementGroup> = INITIAL_ITEMS.apply { first { it.matchType == ALL }.add(start) },
    val action: (suspend IMatchScope.() -> Master?)? = null
) : ConditionElement {

    constructor(start: Master) : this(
        when (start) {
            is Combo<*> -> start
            is AbsEvent -> start.combo
            is AbsState -> start.combo
            is AbsEventGroup<*> -> start.combo
            is AbsStateGroup<*> -> start.combo
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
        is AbsEvent,
        is AbsEventGroup<*> -> EVENT
        is AbsState,
        is AbsStateGroup<*> -> STATE
        is External -> EXTERNAL
        is Combo<*> -> when (master) {
            is AbsEvent,
            is AbsEventGroup<*> -> EVENT
            is AbsState,
            is AbsStateGroup<*> -> STATE
            is External -> EXTERNAL
            else -> throw IllegalArgumentException("Unknown Master of type $this")
        }
        else -> throw IllegalArgumentException("Unknown Master of type $this")
    }

    override fun toString(): String = "C($items)"
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT