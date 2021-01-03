@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.EVENT
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.STATE
import dev.zieger.utils.statemachine.conditionelements.DefinitionGroup.MatchType.*

class EventCondition(
    start: Combo<*>,
    items: List<DefinitionGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(start, items, action) {

    constructor(vararg events: AbsEventType) : this(events.first().combo, INITIAL_ITEMS.apply { any.addAll(events) })

    override fun copy(
        start: Combo<*>,
        items: List<DefinitionGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): EventCondition = EventCondition(start, items, action)
}

class StateCondition(
    start: Combo<*>,
    items: List<DefinitionGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(start, items, action) {

    constructor(vararg states: AbsStateType) : this(states.first().combo, INITIAL_ITEMS.apply { any.addAll(states) })

    override fun copy(
        start: Combo<*>,
        items: List<DefinitionGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): StateCondition = StateCondition(start, items, action)
}

open class Condition(
    val start: Combo<*>,
    private val items: List<DefinitionGroup> = INITIAL_ITEMS.apply { all.add(start) },
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

    constructor(vararg master: Master) : this(master.first().combo, INITIAL_ITEMS.apply { any.addAll(master) })

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

    open fun copy(
        start: Combo<*> = this.start,
        items: List<DefinitionGroup> = this.items,
        action: (suspend IMatchScope.() -> Master?)? = this.action
    ) = Condition(start, items, action)

    override fun equals(other: Any?): Boolean = (other as? Condition)?.let { o ->
        master == o.master
    } == true

    override fun hashCode(): Int = master.hashCode()
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT