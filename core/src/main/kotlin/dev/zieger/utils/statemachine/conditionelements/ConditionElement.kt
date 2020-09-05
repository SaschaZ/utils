package dev.zieger.utils.statemachine.conditionelements


import dev.zieger.utils.misc.name
import dev.zieger.utils.statemachine.MatchScope

/**
 * Base class for [Master]s, [Slave]s, [ComboElement] and [Condition].
 */
abstract class ConditionElement {

    abstract suspend fun MatchScope.match(other: ConditionElement?): Boolean

    override fun toString(): String = this::class.name
}

@Suppress("UNCHECKED_CAST")
val ConditionElement?.noLogging
    get() = ((this as? Event) ?: ((this as? ComboEventElement)?.master))?.noLogging == true