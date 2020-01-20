package de.gapps.utils.observable


interface IOnChangedScope<out T> {
    var value: @UnsafeVariance T
    val previousValue: T?
    val previousValues: List<T>
    val clearPreviousValues: () -> Unit
}

data class OnChangedScope<out T>(
    override var value: @UnsafeVariance T,
    override val previousValue: T?,
    override val previousValues: List<T>,
    override val clearPreviousValues: () -> Unit
) : IOnChangedScope<T>

typealias ChangeObserver<T> = IOnChangedScope<T>.(T) -> Unit