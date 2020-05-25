@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ExternalFilter

import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
import kotlin.reflect.KClass

interface IType<out T : IData> : ISlave {
    val type: KClass<@UnsafeVariance T>

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IData -> type.isInstance(other)
            is IType<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            filters + ExternalFilter(noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            "#T $it => ${this@IType} <||> $other"
        }
    }
}

/**
 * Every [Data] class should implement this companion.
 */
abstract class Type<out T : IData>(override val type: KClass<@UnsafeVariance T>) : Slave(), IType<T> {

    override fun toString(): String = type.name
}