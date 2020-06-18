@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.Matcher.IMatchScope

interface IData : ISlave {

    override suspend fun IMatchScope.match(
        other: IConditionElement?
    ): Boolean {
        return when (other) {
            is IData -> this@IData == other
            is IType<*> -> other.run { match(this@IData) }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@IData::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#D $it => ${this@IData} <||> $other"
        }
    }
}

/**
 * Every data needs to implement this class.
 */
abstract class Data : Slave(), IData