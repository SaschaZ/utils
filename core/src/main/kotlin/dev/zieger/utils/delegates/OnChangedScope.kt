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
interface IOnChangedScopeWithParent<out P : Any?, T : Any?> {
    var value: T
    val thisRef: P?
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
    val isInitialNotification: Boolean
    val valueChangedListener: (T) -> Unit
}

typealias IOnChangedScope<T> = IOnChangedScopeWithParent<Any?, T>

/**
 * Simple implementation of [IOnChangedScopeWithParent].
 */
class OnChangedScopeWithParent<P : Any?, T : Any?>(
    initial: T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit,
    override val isInitialNotification: Boolean = false,
    override val valueChangedListener: (T) -> Unit = {}
) : IOnChangedScopeWithParent<P, T> {

    override var value: T by OnChanged(initial) { valueChangedListener(it) }
}

typealias OnChangedScope<T> = OnChangedScopeWithParent<Any?, T>