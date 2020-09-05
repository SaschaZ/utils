@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.MatchScope

data class InputElement(
    val event: ComboEventElement,
    val state: ComboStateElement
) : ConditionElement() {

    override suspend fun MatchScope.match(other: ConditionElement?): Boolean {
        return when (other) {
            is Condition -> other.run { match(this@InputElement) }
            is ComboBaseElement<*, *> -> when {
                other.hasEvent || other.hasEventGroup -> event.run { match(other) }
                other.hasState || other.hasStateGroup -> state.run { match(other) }
                else -> false
            }
            null -> false
            else -> throw IllegalArgumentException("Can not match ${this@InputElement::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#IE $it => ${this@InputElement} <||> $other"
        }
    }

    override fun toString(): String = "IE($event, $state)"
}