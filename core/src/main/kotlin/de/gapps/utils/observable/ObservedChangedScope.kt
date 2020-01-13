package de.gapps.utils.observable


interface IObservedChangedScope<out T> {
    val value: T
    val previousValue: T?
    val previousValues: List<T>
}

data class ObservedChangedScope<out T>(
    override val value: T,
    override val previousValue: T?,
    override val previousValues: List<T>
) : IObservedChangedScope<T>

typealias ChangeObserver<T> = IObservedChangedScope<T>.(T) -> Unit