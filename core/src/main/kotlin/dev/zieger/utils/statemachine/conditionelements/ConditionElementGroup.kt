@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
import dev.zieger.utils.statemachine.conditionelements.IConditionElementGroup.MatchType
import dev.zieger.utils.statemachine.conditionelements.IConditionElementGroup.MatchType.*

interface IConditionElementGroup : IConditionElement {

    enum class MatchType {
        ALL,
        ANY,
        NONE
    }

    val matchType: MatchType
    val elements: MutableList<IComboElement>

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IInputElement -> {
                val filtered = elements.filter {
                    (it.hasEvent || it.hasEventGroup) && (other.event.hasEvent || other.event.hasEventGroup)
                            || (it.hasState || it.hasStateGroup) && (other.state.hasState || other.state.hasStateGroup)
                            || it.hasExternal && matchType.anyOf(ALL, NONE)
                }
                filtered.isEmpty() || when (matchType) {
                    ALL -> filtered.all { it.match(other, previousStateChanges) }
                    ANY -> filtered.any { it.match(other, previousStateChanges) }
                    NONE -> filtered.none { it.match(other, previousStateChanges) }
                }
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            f =
                GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#CG $it => ${this@IConditionElementGroup} <||> $other"
        }
    }
}

data class ConditionElementGroup(
    override val matchType: MatchType,
    override val elements: MutableList<IComboElement>
) : IConditionElementGroup {
    override fun toString(): String = "CG($matchType; $elements)"
}