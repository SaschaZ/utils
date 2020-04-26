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
interface IOnChangedScope<P : Any?, out T : Any?> {
    val value: @UnsafeVariance T
    val thisRef: P?
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
}

/**
 * Simple implementation of [IOnChangedScope].
 */
data class OnChangedScope<P : Any?, out T : Any?>(
    override val value: @UnsafeVariance T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit
) : IOnChangedScope<P, T>