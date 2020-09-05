@file:Suppress("RemoveCurlyBracesFromTemplate", "unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.MatchScope
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*
import dev.zieger.utils.statemachine.conditionelements.DefinitionElementGroup.MatchType.*

data class Condition(
    val items: List<DefinitionElementGroup> = INITIAL_ITEMS,
    val action: (suspend MatchScope.() -> ComboStateElement?)? = null
) : ConditionElement() {

    constructor(event: Event) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(event.comboEvent) })

    constructor(state: State) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(state.comboState) })

    constructor(eventGroup: EventGroup<*>) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(eventGroup.comboEventGroup) })

    constructor(stateGroup: StateGroup<*>) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(stateGroup.comboStateGroup) })

    constructor(combo: ComboBaseElement<Master, Slave>) :
            this(INITIAL_ITEMS.apply { first { it.matchType == ALL }.elements.add(combo) })

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

    val start: DefinitionElement get() = items.first { it.matchType == ALL }.elements.first()

    enum class DefinitionType {
        STATE,
        EVENT,
        EXTERNAL
    }

    val type: DefinitionType
        get() = start.type

    private fun Master.getType(): DefinitionType = when (this) {
        is Event,
        is EventGroup<*> -> EVENT
        is State,
        is StateGroup<*> -> STATE
        is External -> EXTERNAL
        is ComboBaseElement<*, *> -> master.getType()
    }

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is InputElement -> {
                any.run { match(other) }
                        && all.run { match(other) }
                        && none.run { match(other) }
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@Condition::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#C $it => ${this@Condition} <||> $other"
        }
    }

    override fun toString(): String = "C($items)"
}

val Condition.isStateCondition get() = type == STATE
val Condition.isEventCondition get() = type == EVENT