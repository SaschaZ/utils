package de.gapps.utils.observable


interface IControlledChangedScope<P : Any?, out T> : IOnChangedScope<P, T> {

    override var value: @UnsafeVariance T
}

class ControlledChangedScope<P : Any?, out T : Any?>(
    current: T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    private val onClearRecentValues: () -> Unit,
    private val onNewValue: (T) -> Unit
) : IControlledChangedScope<P, T> {

    override var value: @UnsafeVariance T = current
        set(newValue) = onNewValue(newValue)

    override val clearPreviousValues: () -> Unit = { onClearRecentValues() }
}