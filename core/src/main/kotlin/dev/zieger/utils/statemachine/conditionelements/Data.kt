@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ExternalFilter

import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged

interface IData : ISlave {

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IData -> this == other
            is IType<*> -> other.match(this, previousStateChanges)
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            filters + ExternalFilter(noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            "#D $it => ${this@IData} <||> $other"
        }
    }
}

/**
 * Every data needs to implement this class.
 */
abstract class Data : Slave(), IData