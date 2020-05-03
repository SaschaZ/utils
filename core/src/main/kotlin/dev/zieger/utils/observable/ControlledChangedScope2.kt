package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedScope2


interface IControlledChangedScope<out T : Any?> : IControlledChangedScope2<Any?, T>

interface IControlledChangedScope2<P : Any?, out T : Any?> : IOnChangedScope2<P, T> {

    override var value: @UnsafeVariance T
}

open class ControlledChangedScope<out T : Any?>(
    current: T,
    thisRef: Any?,
    previousValue: T?,
    previousValues: List<T?>,
    onClearRecentValues: () -> Unit,
    onNewValue: (T) -> Unit
) : ControlledChangedScope2<Any?, T>(
    current, thisRef, previousValue, previousValues, onClearRecentValues, onNewValue
), IControlledChangedScope<T>

open class ControlledChangedScope2<P : Any?, out T : Any?>(
    current: T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    private val onClearRecentValues: () -> Unit,
    private val onNewValue: (T) -> Unit
) : IControlledChangedScope2<P, T> {

    override var value: @UnsafeVariance T = current
        set(newValue) = onNewValue(newValue)

    override val clearPreviousValues: () -> Unit = { onClearRecentValues() }
}