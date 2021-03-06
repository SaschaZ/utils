@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter.Companion.GENERIC
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.Matcher.IMatchScope
import dev.zieger.utils.statemachine.conditionelements.UsedAs.DEFINITION
import dev.zieger.utils.statemachine.conditionelements.UsedAs.RUNTIME

interface IComboElement : IConditionElement, IActionResult {

    val master: IMaster
    var slave: ISlave?
    var usedAs: UsedAs
    val ignoreSlave: Boolean
    var exclude: Boolean

    val event get() = master as? IEvent
    val state get() = master as? State
    val group get() = master as? IGroup<*>
    val eventGroup get() = master as? IEventGroup<*>
    val stateGroup get() = master as? IStateGroup<*>
    val external get() = master as? IExternal

    val hasEvent get() = event != null
    val hasState get() = state != null
    val hasGroup get() = group != null
    val hasStateGroup get() = stateGroup != null
    val hasEventGroup get() = eventGroup != null
    val hasExternal get() = external != null

    override suspend fun IMatchScope.match(
        other: IConditionElement?
    ): Boolean {
        return when {
            hasExternal -> external?.run { match(other) } ?: false
            other is IComboElement -> {
                when {
                    other.hasExternal -> other.external?.run { match(this@IComboElement) } ?: false
                    else -> master.run { match(other.master) }
                            && (slave == null && other.slave == null
                            || ignoreSlave || other.ignoreSlave
                            || slave?.run { match(other.slave) } == true)
                }
            }
            other is IInputElement -> other.run { match(this@IComboElement) }
            other == null -> false
            else -> throw IllegalArgumentException("Can not match ${this@IComboElement::class.name} " +
                    "with ${other.let { it::class.name }}"
            )
        } logV {
            f = GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
            m = "#CE $it => ${this@IComboElement} <||> $other"
        }
    }
}

data class ComboElement(
    override val master: IMaster,
    override var slave: ISlave? = null,
    override var usedAs: UsedAs = DEFINITION,
    override val ignoreSlave: Boolean = false,
    override var exclude: Boolean = false
) : IComboElement {
    override fun toString() = "CE($master|$slave|$ignoreSlave|$exclude|${when (master) {
        is IEvent -> "E"
        is State -> "S"
        is IEventGroup<*> -> "Eg"
        is IStateGroup<*> -> "Sg"
        is IExternal -> "X"
        else -> "?[${master::class}]"
    }}${usedAs.name[0]})"
}

val IComboElement.isDefinition get() = usedAs == DEFINITION
val IComboElement.isRuntime get() = usedAs == RUNTIME