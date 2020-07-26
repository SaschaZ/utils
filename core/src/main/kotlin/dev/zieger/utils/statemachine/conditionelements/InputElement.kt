@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.IMatchScope
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO

interface IInputElement : IConditionElement {

    val event: IComboElement
    val state: IComboElement

    override suspend fun IMatchScope.match(other: IConditionElement?): Boolean {
        return when (other) {
            is ICondition -> other.run { match(this@IInputElement) }
            is IComboElement -> when {
                other.hasEvent || other.hasEventGroup -> event.run { match(other) }
                other.hasState || other.hasStateGroup -> state.run { match(other) }
                else -> false
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@IInputElement::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
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