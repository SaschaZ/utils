package dev.zieger.utils.statemachine.conditionelements

import kotlin.reflect.KClass

/**
 * Type that can be combined added to [Master].
 */
interface Slave : ConditionElement

/**
 * Every data needs to implement this class.
 */
interface Data : Slave

/**
 * Every [Data] class should implement this companion.
 */
open class Type<T : Data>(val type: KClass<T>) : Slave {

    override fun toString(): String = type.simpleName!!
}