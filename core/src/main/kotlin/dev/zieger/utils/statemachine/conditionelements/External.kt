package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.MachineEx

/**
 * External condition.
 * Is checked at runtime. All External's need to match within a condition.
 */
open class External(private val condition: suspend IMatchScope.() -> Boolean) : Definition {

    suspend fun matchExternal(scope: IMatchScope): Boolean = scope.run {
        condition() logV {
            f = LogFilter.Companion.GENERIC(
                disableLog = noLogging
                        || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO
            )
            m = "#EX $it => ${this@External}"
        }
    }

    override val hasExternal = true

    override fun toString(): String = "External"
}