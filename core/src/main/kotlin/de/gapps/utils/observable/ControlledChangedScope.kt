package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged


interface IControlledChangedScope<out T> : IObservedChangedScope<T> {

    override var value: @UnsafeVariance T
}

class ControlledChangedScope<out T>(
    current: T,
    override val previousValue: T?,
    override val previousValues: List<T>,
    onChanged: T.(T) -> Unit
) : IControlledChangedScope<T> {
    override var value: @UnsafeVariance T by OnChanged(current) { onChanged(it) }
}

typealias ControlObserver<T> = IControlledChangedScope<T>.(T) -> Unit