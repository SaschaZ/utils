@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.Matcher.IMatchScope
import kotlin.reflect.KClass

interface IStateGroup<out T : State> : IGroup<T> {

    override suspend fun IMatchScope.match(other: IConditionElement?): Boolean {
        return when (other) {
            is State -> type.isInstance(other)
            is IStateGroup<*> -> other.type == type
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@IStateGroup::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#SG $it => ${this@IStateGroup} <||> $other"
        }
    }
}

abstract class StateGroup<out T : State>(type: KClass<T>) :
    Group<@UnsafeVariance T>(type),
    IStateGroup<T> {
    override fun toString(): String = type.name
}