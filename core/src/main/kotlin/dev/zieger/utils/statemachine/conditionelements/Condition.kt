@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ExternalFilter

import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.ExecutorScope
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
import dev.zieger.utils.statemachine.conditionelements.ICondition.ConditionType.*
import dev.zieger.utils.statemachine.conditionelements.IConditionElementGroup.MatchType.*

interface ICondition : IConditionElement {

    val items: List<IConditionElementGroup>
    val any: IConditionElementGroup get() = items.first { it.matchType == ANY }
    val all: IConditionElementGroup get() = items.first { it.matchType == ALL }
    val none: IConditionElementGroup get() = items.first { it.matchType == NONE }
    val action: (suspend ExecutorScope.() -> IComboElement?)?

    val start: IComboElement get() = items.first { it.matchType == ALL }.elements.first()

    enum class ConditionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: ConditionType
        get() = when (start.master) {
            is IEvent,
            is IEventGroup<IEvent> -> EVENT
            is State,
            is IStateGroup<State> -> STATE
            is IExternal -> EXTERNAL
            else -> throw IllegalArgumentException("Unexpected first element $start")
        }

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IInputElement -> {
                any.match(other, previousStateChanges)
                        && all.match(other, previousStateChanges)
                        && none.match(other, previousStateChanges)
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            elements + ExternalFilter(noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            "#C $it => ${this@ICondition} <||> $other"
        }
    }
}

data class Condition(
    override val items: List<IConditionElementGroup> = INITIAL_ITEMS,
    override val action: (suspend ExecutorScope.() -> IComboElement?)? = null
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