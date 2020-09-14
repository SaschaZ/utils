package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MatchScope

/**
 * External condition.
 * Is checked at runtime. All External's need to match within a condition.
 */
open class External(val condition: suspend IMatchScope.() -> Boolean) : DefinitionElement {

    suspend fun MatchScope.match2(other: ConditionElement?): Boolean =
        condition() logV {
            f = LogFilter.Companion.GENERIC(
                disableLog = noLogging || other.noLogging
                        || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO
            )
            m = "#EX $it => ${this@External} <||> $other"
        }

    override val hasExternal = true

    override fun toString(): String = "External"
}