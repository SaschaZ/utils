@file:Suppress("unused")

package dev.zieger.utils.delegates


/**
 * Same as [IOnChangedScope2] but with [Any]? as parent type.
 */
interface IOnChangedScope<out T : Any?> : IOnChangedScope2<Any?, T>

/**
 * Scope that describes the change of any property of type [T] hold by a parent of type [P].
 *
 * @property value new value of changed property
 * @property thisRef parent that holds the property
 * @property previousValue value of the property before it changed to [value]
 * @property previousValues all previous values of the property
 * @property clearPreviousValues Lambda to clear the previous values
 */
interface IOnChangedScope2<P : Any?, out T : Any?> {
    val value: @UnsafeVariance T
    val thisRef: P?
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
}

/**
 * Same as [OnChangedScope2] but with [Any]? as parent type.
 */
open class OnChangedScope<out T : Any?>(
    value: @UnsafeVariance T,
    thisRef: Any?,
    previousValue: T?,
    previousValues: List<T?>,
    clearPreviousValues: () -> Unit
) : OnChangedScope2<Any?, T>(
    value, thisRef, previousValue, previousValues, clearPreviousValues
), IOnChangedScope<T>

/**
 * Simple implementation of [IOnChangedScope2].
 */
open class OnChangedScope2<P : Any?, out T : Any?>(
    override val value: @UnsafeVariance T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit
) : IOnChangedScope2<P, T>