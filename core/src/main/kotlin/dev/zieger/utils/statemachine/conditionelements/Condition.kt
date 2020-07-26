@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.*
import dev.zieger.utils.statemachine.conditionelements.IConditionElementGroup.MatchType.*

interface ICondition : IConditionElement {

    val items: List<IConditionElementGroup>
    val any: IConditionElementGroup get() = items.first { it.matchType == ANY }
    val all: IConditionElementGroup get() = items.first { it.matchType == ALL }
    val none: IConditionElementGroup get() = items.first { it.matchType == NONE }
    val action: (suspend IMatchScope.() -> IComboElement?)?

    val start: IComboElement get() = items.first { it.matchType == ALL }.elements.first()

    enum class ConditionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: ConditionType
        get() = start.master.getType()

    private fun IMaster.getType(): ConditionType = when (this) {
        is IEvent,
        is IEventGroup<IEvent> -> EVENT
        is IState,
        is IStateGroup<IState> -> STATE
        is IExternal -> EXTERNAL
        is IComboElement -> master.getType()
        else -> throw IllegalArgumentException("Unexpected first element $start")
    }

    override suspend fun IMatchScope.match(other: IConditionElement?): Boolean {
        return when (other) {
            is IInputElement -> {
                any.run { match(other) }
                        && all.run { match(other) }
                        && none.run { match(other) }
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@ICondition::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#C $it => ${this@ICondition} <||> $other"
        }
    }
}

data class Condition(
    override val items: List<IConditionElementGroup> = INITIAL_ITEMS,
    override val action: (suspend IMatchScope.() -> IComboElement?)? = null
) : ICondition {

    constructor(master: IMaster) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(master.combo) })

    companion object {
        private val INITIAL_ITEMS
            get() = listOf(
                ConditionElementGroup(ANY, ArrayList()),
                ConditionElementGroup(ALL, ArrayList()),
                ConditionElementGroup(NONE, ArrayList())
            )
    }

    override fun toString(): String = "C($items)"
}

val ICondition.isStateCondition get() = type == STATE
val ICondition.isEventCondition get() = type == EVENT