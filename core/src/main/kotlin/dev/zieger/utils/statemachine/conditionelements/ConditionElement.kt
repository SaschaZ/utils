package dev.zieger.utils.statemachine.conditionelements


/**
 * Base class for [Master]s, [Slave]s, [ComboElement] and [Condition].
 */
interface ConditionElement

@Suppress("UNCHECKED_CAST")
val Any?.noLogging
    get() = (this as? Event)?.noLogging == true