package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged


interface IControlledChangedScope<out T> : IOnChangedScope<T> {

    override var value: @UnsafeVariance T
}

class ControlledChangedScope<out T>(
    current: T,
    override val previousValue: T?,
    override val previousValues: List<T>,
    onChanged: ChangeObserver<T>
) : IControlledChangedScope<T> {
    override var value: @UnsafeVariance T by OnChanged(current) { onChanged(it) }

    override val clearPreviousValues: () -> Unit = {} // TODO implement or change pattern
}

typealias ControlObserver<T> = IControlledChangedScope<T>.(T) -> Unit