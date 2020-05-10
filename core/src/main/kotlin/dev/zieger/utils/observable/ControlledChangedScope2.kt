package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.IOnChangedScope2


interface IControlledChangedScope<out T : Any?> : IControlledChangedScope2<Any?, T>, IOnChangedScope<T>

interface IControlledChangedScope2<P : Any?, out T : Any?> : IOnChangedScope2<P, T> {

    override var value: @UnsafeVariance T
}

open class ControlledChangedScope<out T : Any?>(
    current: T,
    thisRef: Any?,
    previousValue: T?,
    previousValues: List<T?>,
    onClearRecentValues: () -> Unit,
    onNewValue: (T) -> Unit,
    isInitialNotification: Boolean = false
) : ControlledChangedScope2<Any?, T>(
    current, thisRef, previousValue, previousValues, onClearRecentValues, onNewValue, isInitialNotification
), IControlledChangedScope<T>

open class ControlledChangedScope2<P : Any?, out T : Any?>(
    current: T,
    override val thisRef: P?,
    override val previousValue: T?,
    override val previousValues: List<T?>,
    private val onClearRecentValues: () -> Unit,
    private val onNewValue: (T) -> Unit,
    override val isInitialNotification: Boolean = false
) : IControlledChangedScope2<P, T> {

    override var value: @UnsafeVariance T = current
        set(newValue) = onNewValue(newValue)

    override val clearPreviousValues: () -> Unit = { onClearRecentValues() }
}