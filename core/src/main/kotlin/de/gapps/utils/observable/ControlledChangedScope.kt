package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChangedScope


interface IControlledChangedScope<out P : Any?, out T> : IOnChangedScope<@UnsafeVariance P, T> {

    override var value: @UnsafeVariance T
}

class ControlledChangedScope<out P : Any?, out T : Any?>(
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