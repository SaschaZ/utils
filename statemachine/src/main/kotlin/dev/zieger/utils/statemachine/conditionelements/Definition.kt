@file:Suppress("unused")

package dev.zieger.utils.statemachine.conditionelements

import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType
import dev.zieger.utils.statemachine.conditionelements.Condition.DefinitionType.*

interface Definition : ConditionElement {

    val hasEvent: Boolean get() = false
    val hasState: Boolean get() = false
    val hasStateGroup: Boolean get() = false
    val hasEventGroup: Boolean get() = false
    val hasExternal: Boolean get() = false
    val hasPrevious: Boolean get() = false

    @Suppress("LeakingThis")
    val hasGroup: Boolean
        get() = hasStateGroup || hasEventGroup

    val type: DefinitionType
        get() = when {
            hasState || hasStateGroup -> STATE
            hasEvent || hasEventGroup -> EVENT
            hasExternal -> EXTERNAL
            else -> throw IllegalArgumentException("Can not build DefinitionType for $this")
        }
}
