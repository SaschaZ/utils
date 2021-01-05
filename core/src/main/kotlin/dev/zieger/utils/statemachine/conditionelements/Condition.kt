@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.runEach
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE
import dev.zieger.utils.statemachine.conditionelements.DefinitionGroup.MatchType.*

class EventCondition(
    items: List<DefinitionGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(items, action) {

    constructor(vararg events: AbsEventType) :
            this(INITIAL_ITEMS.apply { any.addAll(events) })

    override fun copy(
        items: List<DefinitionGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): EventCondition = EventCondition(items, action)
}

class StateCondition(
    items: List<DefinitionGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(items, action) {

    constructor(vararg states: AbsStateType) :
            this(INITIAL_ITEMS.apply { any.addAll(states) })

    override fun copy(
        items: List<DefinitionGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): StateCondition = StateCondition(items, action)
}

class RawCondition(
    items: List<DefinitionGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(items, action) {
    override fun copy(
        items: List<DefinitionGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): Condition = RawCondition(items, action)
}

sealed class Condition(
    private val items: List<DefinitionGroup> = INITIAL_ITEMS,
    val action: (suspend IMatchScope.() -> Master?)? = null
) : ConditionElement {

    constructor(vararg master: Master) :
            this(INITIAL_ITEMS.apply { all.addAll(master.toList().runEach { combo }) })

    companion object {
        internal val INITIAL_ITEMS
            get() = listOf(
                DefinitionGroup(ANY, ArrayList()),
                DefinitionGroup(ALL, ArrayList()),
                DefinitionGroup(NONE, ArrayList())
            )

        internal val List<DefinitionGroup>.any get() = first { it.matchType == ANY }
        internal val List<DefinitionGroup>.all get() = first { it.matchType == ALL }
        internal val List<DefinitionGroup>.none get() = first { it.matchType == NONE }
    }

    val start: Combo<*> get() = (items.all.first() as Master).combo

    internal val any: DefinitionGroup get() = items.any
    internal val all: DefinitionGroup get() = items.all
    internal val none: DefinitionGroup get() = items.none

    enum class DefinitionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: DefinitionType
        get() = start.type

    override fun toString(): String = "C($items)"

    abstract fun copy(
        items: List<DefinitionGroup> = this.items,
        action: (suspend IMatchScope.() -> Master?)? = this.action
    ): Condition

    override fun equals(other: Any?): Boolean = (other as? Condition)?.let { o ->
        master == o.master
    } == true

    override fun hashCode(): Int = master.hashCode()
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT