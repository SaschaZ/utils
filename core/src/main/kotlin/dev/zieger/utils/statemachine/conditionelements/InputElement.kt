@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged

interface IInputElement : IConditionElement {

    val event: IComboElement
    val state: IComboElement

    override suspend fun match(other: IConditionElement?, previousStateChanges: List<OnStateChanged>): Boolean {
        return when (other) {
            is ICondition -> other.match(this, previousStateChanges)
            is IComboElement -> when {
                other.hasEvent || other.hasEventGroup -> event.match(other, previousStateChanges)
                other.hasState || other.hasStateGroup -> state.match(other, previousStateChanges)
                else -> false
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            f =
                GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#IE $it => ${this@IInputElement} <||> $other"
        }
    }
}

data class InputElement(
    override val event: IComboElement,
    override val state: IComboElement
) : IInputElement {
    override fun toString(): String = "IE($event, $state)"
}