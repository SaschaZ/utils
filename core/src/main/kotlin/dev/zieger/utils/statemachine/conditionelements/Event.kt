@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged

interface IEvent : ISingle {
    val noLogging: Boolean
    fun OnStateChanged.fired() = Unit

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when (other) {
            is IEvent -> this === other
            is IEventGroup<IEvent> -> other.match(this, previousStateChanges)
            else -> false
        } logV {
            f =
                GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#E $it => ${this@IEvent} <||> $other"
        }
    }
}

/**,
 * All events need to implement this class.
 * @property noLogging When `true` log messages for this [Event] are not printed. Default is `false`.
 */
abstract class Event(
    override val noLogging: Boolean = false
) : Single(), IEvent