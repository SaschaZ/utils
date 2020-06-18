@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.Matcher.IMatchScope
import kotlin.reflect.KClass

interface IType<out T : IData> : ISlave {
    val type: KClass<@UnsafeVariance T>

    override suspend fun IMatchScope.match(other: IConditionElement?): Boolean {
        return when (other) {
            is IData -> type.isInstance(other)
            is IType<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@IType::class.name}" +
                    " with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#T $it => ${this@IType} <||> $other"
        }
    }
}

/**
 * Every [Data] class should implement this companion.
 */
abstract class Type<out T : IData>(override val type: KClass<@UnsafeVariance T>) : Slave(), IType<T> {

    override fun toString(): String = type.name
}