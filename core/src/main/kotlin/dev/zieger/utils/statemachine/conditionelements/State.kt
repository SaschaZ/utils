@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements


import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged

interface IState : ISingle, IActionResult {
    fun OnStateChanged.activeStateChanged(isActive: Boolean) = Unit

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is State -> this === other
            is IStateGroup<State> -> other.match(this, previousStateChanges)
            else -> false
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#ST $it => ${this@IState} <||> $other"
        }
    }
}

/**
 * All states need to implement this class.
 */
abstract class State : Single(), IState