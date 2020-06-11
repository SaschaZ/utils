@file:Suppress("RemoveCurlyBracesFromTemplate")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logV
import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MachineEx
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.INFO
import dev.zieger.utils.statemachine.OnStateChanged
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

    override suspend fun match(
        other: IConditionElement?,
        previousStateChanges: List<OnStateChanged>
    ): Boolean {
        return when {
            hasExternal -> external?.match(other, previousStateChanges) ?: false
            other is IComboElement -> {
                when {
                    other.hasExternal -> other.external?.match(this, previousStateChanges) ?: false
                    else -> master.match(other.master, previousStateChanges)
                            && (slave == null && other.slave == null
                            || ignoreSlave || other.ignoreSlave
                            || slave?.match(other.slave, previousStateChanges) == true)
                }
            }
            other is IInputElement -> other.match(this, previousStateChanges)
            other == null -> false
            else -> throw IllegalArgumentException("Can not match ${this::class.name} with ${other.let { it::class.name }}")
        } logV {
            logFilter =
                LogFilter.Companion.GENERIC(disableLog = noLogging || other.noLogging || MachineEx.debugLevel <= INFO)
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