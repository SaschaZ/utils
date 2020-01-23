package de.gapps.utils.observable


interface IControlledChangedScope<out T> : IOnChangedScope<Any?, T> {

    override var value: @UnsafeVariance T
}

class ControlledChangedScope<out T : Any?>(
    current: T,
    override val thisRef: Any?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    private val onClearRecentValues: () -> Unit,
    private val onNewValue: (T) -> Unit
) : IControlledChangedScope<T> {

    override var value: @UnsafeVariance T = current
        set(newValue) = onNewValue(newValue)

    override val clearPreviousValues: () -> Unit = { onClearRecentValues() }
}