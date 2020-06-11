@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ExternalFilter

import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.MatchScope
import dev.zieger.utils.statemachine.OnStateChanged

interface IExternal : ISingle {

    val condition: suspend MatchScope.() -> Boolean

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean =
        MatchScope(previousStateChanges).condition() logV {
            elements + ExternalFilter(noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            "#EX $it => ${this@IExternal} <||> $other"
        }
}

/**
 * External condition.
 * Is checked at runtime. All External's need to match within a condition.
 */
open class External(override val condition: suspend MatchScope.() -> Boolean) :
    Single(), IExternal {
    override fun toString(): String = "External"
}