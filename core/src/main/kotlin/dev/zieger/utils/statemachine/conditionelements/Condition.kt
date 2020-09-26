@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*
import dev.zieger.utils.statemachine.conditionelements.DefinitionElementGroup.MatchType.*

class EventCondition(
    start: Combo<*>,
    items: List<DefinitionElementGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(start, items, action) {

    constructor(vararg events: AbsEventType) : this(events.first().combo, INITIAL_ITEMS.apply { any.addAll(events) })

    override fun copy(
        start: Combo<*>,
        items: List<DefinitionElementGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): EventCondition = EventCondition(start, items, action)
}

class StateCondition(
    start: Combo<*>,
    items: List<DefinitionElementGroup>,
    action: (suspend IMatchScope.() -> Master?)? = null
) : Condition(start, items, action) {

    constructor(vararg states: AbsStateType) : this(states.first().combo, INITIAL_ITEMS.apply { any.addAll(states) })

    override fun copy(
        start: Combo<*>,
        items: List<DefinitionElementGroup>,
        action: (suspend IMatchScope.() -> Master?)?
    ): StateCondition = StateCondition(start, items, action)
}

@Suppress("DataClassPrivateConstructor")
open class Condition(
    val start: Combo<*>,
    val items: List<DefinitionElementGroup> = INITIAL_ITEMS.apply { all.add(start) },
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
                DefinitionElementGroup(ANY, ArrayList()),
                DefinitionElementGroup(ALL, ArrayList()),
                DefinitionElementGroup(NONE, ArrayList())
            )

        internal val List<DefinitionElementGroup>.any get() = first { it.matchType == ANY }
        internal val List<DefinitionElementGroup>.all get() = first { it.matchType == ALL }
        internal val List<DefinitionElementGroup>.none get() = first { it.matchType == NONE }
    }

    internal val any: DefinitionElementGroup get() = items.any
    internal val all: DefinitionElementGroup get() = items.all
    internal val none: DefinitionElementGroup get() = items.none

    enum class DefinitionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: DefinitionType
        get() = start.type

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

    open fun copy(
        start: Combo<*> = this.start,
        items: List<DefinitionElementGroup> = this.items,
        action: (suspend IMatchScope.() -> Master?)? = this.action
    ) = Condition(start, items, action)

    override fun equals(other: Any?): Boolean = (other as? Condition)?.let { o ->
        master == o.master
    } == true

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT