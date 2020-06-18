package dev.zieger.utils.statemachine.conditionelements


import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.Matcher.IMatchScope


interface IConditionElement {

    suspend fun IMatchScope.match(other: IConditionElement?): Boolean
}

/**
 * Base class for [IMaster]s, [ISlave]s, [IComboElement] and [ICondition].
 */
abstract class ConditionElement : IConditionElement {

    override fun toString(): String = this::class.name
}

val IConditionElement?.noLogging
    get() = ((this as? IEvent) ?: ((this as? IComboElement)?.event))?.noLogging == true