package dev.zieger.utils.delegates


interface IOnChangedScope<P : Any?, out T : Any?> {
    val value: @UnsafeVariance T
    val thisRef: P?
    val previousValue: T?
    val previousValues: List<T?>
    val clearPreviousValues: () -> Unit
}

data class OnChangedScope<P : Any?, out T : Any?>(
    override val value: @UnsafeVariance T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    override val clearPreviousValues: () -> Unit
) : IOnChangedScope<P, T>