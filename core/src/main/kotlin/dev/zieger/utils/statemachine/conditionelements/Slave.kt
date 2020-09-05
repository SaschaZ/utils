package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MatchScope
import kotlin.reflect.KClass

/**
 * Type that can be combined added to [Master].
 */
sealed class Slave : ConditionElement()

/**
 * Every data needs to implement this class.
 */
abstract class Data : Slave() {

    override suspend fun MatchScope.match(
        other: ConditionElement?
    ): Boolean {
        return when (other) {
            is Data -> this@Data == other
            is Type<*> -> other.run { match(this@Data) }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@Data::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#D $it => ${this@Data} <||> $other"
        }
    }
}

/**
 * Every [Data] class should implement this companion.
 */
abstract class Type<T : Data>(val type: KClass<T>) : Slave() {

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is Data -> type.isInstance(other)
            is Type<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@Type::class.name}" +
                    " with ${other.let { it::class.name }}"
            )
        } logV {
            f =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= MachineEx.Companion.DebugLevel.INFO)
            m = "#T $it => ${this@Type} <||> $other"
        }
    }

    override fun toString(): String = type.name
}