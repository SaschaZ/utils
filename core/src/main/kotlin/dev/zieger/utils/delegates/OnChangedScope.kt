@file:Suppress("unused")

package dev.zieger.utils.delegates

/**
 * Scope that describes the change of any property of type [T] hold by a parent of type [P].
 *
 * @property value new value of changed property
 * @property thisRef parent that holds the property
 * @property previousValue value of the property before it changed to [value]
 * @property previousValues all previous values of the property
 * @property clearPreviousValues Lambda to clear the previous values
 */
interface IOnChangedScope2<out P : Any?, out T : Any?> {
    val value: T
    val thisRef: P?
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
    val isInitialNotification: Boolean
}

typealias IOnChangedScope<T> = IOnChangedScope2<Any?, T>

/**
 * Simple implementation of [IOnChangedScope2].
 */
data class OnChangedScope2<P : Any?, out T : Any?>(
    override val value: T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit,
    override val isInitialNotification: Boolean = false
) : IOnChangedScope2<P, T>

typealias OnChangedScope<T> = OnChangedScope2<Any?, T>