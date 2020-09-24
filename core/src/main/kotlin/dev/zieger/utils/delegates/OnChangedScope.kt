@file:Suppress("unused")

package dev.zieger.utils.delegates

import kotlin.properties.Delegates

/**
 * Scope that describes the change of any property of type [T] hold by a parent of type [P].
 *
 * @property value new value of changed property
 * @property parent parent that holds the property
 * @property propertyName name of the property
 * @property previousValue value of the property before it changed to [value]
 * @property previousValues all previous values of the property, when storing of previous values is enabled
 * @property clearPreviousValues Lambda to clear the previous values
 */
interface IOnChangedScopeWithParent<out P : Any?, T : Any?> : IOnChangedScope<T> {
    val parent: P?
}

/**
 * Scope that describes the change of any property of type [T].
 *
 * @property value new value of changed property
 * @property propertyName name of the property
 * @property previousValue value of the property before it changed to [value]
 * @property previousValues all previous values of the property, when storing of previous values is enabled
 * @property clearPreviousValues Lambda to clear the previous values
 */
interface IOnChangedScope<T> {
    var value: T
    val propertyName: String
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
    val isInitialNotification: Boolean
    val valueChangedListener: (T) -> Unit
}

/**
 * Simple implementation of [IOnChangedScopeWithParent].
 */
class OnChangedScopeWithParent<P : Any?, T : Any?>(
    initial: T,
    override val parent: P?,
    override val propertyName: String,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit,
    override val isInitialNotification: Boolean,
    override val valueChangedListener: (T) -> Unit
) : IOnChangedScopeWithParent<P, T> {

    override var value: T by Delegates.observable(initial) { _, _, newValue -> valueChangedListener(newValue) }

    override fun toString(): String = "OnChangedScopeWithParent(value: $value; parent: $parent; " +
            "propertyName: $propertyName; previousValue: $previousValue; previousValues: $previousValues; " +
            "isInitialNotification: $isInitialNotification)"
}

/**
 * Simple implementation of [IOnChangedScope].
 */
class OnChangedScope<T>(
    initial: T,
    override val propertyName: String,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit,
    override val isInitialNotification: Boolean,
    override val valueChangedListener: (T) -> Unit
) : IOnChangedScope<T> {

    override var value: T by Delegates.observable(initial) { _, _, newValue -> valueChangedListener(newValue) }

    override fun toString(): String = "OnChangedScopeWithParent(value: $value; " +
            "propertyName: $propertyName; previousValue: $previousValue; previousValues: $previousValues; " +
            "isInitialNotification: $isInitialNotification)"
}