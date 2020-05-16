@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
import kotlin.reflect.KClass

interface IEventGroup<out T : IEvent> : IGroup<T> {

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IEvent -> type.isInstance(other)
            is IEventGroup<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            f =
                GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#EG $it => ${this@IEventGroup} <||> $other"
        }
    }
}

abstract class EventGroup<out T : IEvent>(type: KClass<T>) :
    Group<@UnsafeVariance T>(type),
    IEventGroup<T> {
    override fun toString(): String = type.name
}