@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.ExternalFilter

import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
import kotlin.reflect.KClass

interface IStateGroup<out T : State> : IGroup<T> {

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is State -> type.isInstance(other)
            is IStateGroup<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            elements + ExternalFilter(noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            "#SG $it => ${this@IStateGroup} <||> $other"
        }
    }
}

abstract class StateGroup<out T : State>(type: KClass<T>) :
    Group<@UnsafeVariance T>(type),
    IStateGroup<T> {
    override fun toString(): String = type.name
}