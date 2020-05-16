package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.misc.name

interface IMaster : IConditionElement

/**
 * Base class for [IEvent]s and [State]s.
 */
abstract class Master : ConditionElement(), IMaster {

    override fun toString(): String = this::class.name
}

val IMaster.combo get() = ComboElement(this)