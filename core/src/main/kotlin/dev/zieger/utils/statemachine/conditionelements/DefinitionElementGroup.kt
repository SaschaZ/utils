@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.MatchScope
import dev.zieger.utils.statemachine.conditionelements.DefinitionElementGroup.MatchType.*

data class DefinitionElementGroup(
    val matchType: MatchType,
    val elements: MutableList<DefinitionElement>
) : ConditionElement(), MutableList<DefinitionElement> by elements {

    enum class MatchType {
        ALL,
        ANY,
        NONE
    }

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is InputElement -> {
                val filtered = elements.filter {
                    (it.hasEvent || it.hasEventGroup) && (other.event.hasEvent || other.event.hasEventGroup)
                            || (it.hasState || it.hasStateGroup) && (other.state.hasState || other.state.hasStateGroup)
                            || it.hasExternal && matchType.anyOf(ALL, NONE)
                }
                filtered.isEmpty() || when (matchType) {
                    ALL -> filtered.all { it.run { match(other) } }
                    ANY -> filtered.any { it.run { match(other) } }
                    NONE -> filtered.none { it.run { match(other) } }
                }
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@DefinitionElementGroup::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#CG $it => ${this@DefinitionElementGroup} <||> $other"
        }
    }

    override fun toString(): String = "CG($matchType; $elements)"
}