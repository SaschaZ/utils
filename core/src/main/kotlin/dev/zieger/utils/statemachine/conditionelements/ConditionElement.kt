package dev.zieger.utils.statemachine.conditionelements


/**
 * Base class for [Master]s, [Slave]s, [Combo] and [Condition].
 */
interface ConditionElement

@Suppress("UNCHECKED_CAST")
val Any?.noLogging
    get() = (this as? AbsEvent)?.noLogging == true