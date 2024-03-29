package dev.zieger.utils.observables

import kotlinx.coroutines.Job

interface IObservableChangedScope<T> {
    val current: T
    val previous: T?
    val previousValues: List<T>
    val clearPreviousValues: () -> Unit
    var unObserve: suspend () -> Unit
}

open class ObservableChangedScope<T>(
    override val current: T,
    override val previous: T?,
    override val previousValues: List<T>,
    override val clearPreviousValues: () -> Unit,
    override var unObserve: suspend () -> Unit
) : IObservableChangedScope<T>

interface IOwnedObservableChangedScope<O, T> : IObservableChangedScope<T> {
    val owner: O?
}

open class OwnedObservableChangedScope<O, T>(
    override val owner: O?,
    current: T,
    previous: T,
    previousValues: List<T>,
    clearPreviousValues: () -> Unit,
    unObserve: suspend () -> Unit
) : IOwnedObservableChangedScope<O, T>,
    ObservableChangedScope<T>(current, previous, previousValues, clearPreviousValues, unObserve)

interface IMutableObservableChangedScope<T> : IObservableChangedScope<T> {
    val offerValue: (T) -> Unit
}

open class MutableObservableChangedScope<T>(
    current: T,
    previous: T?,
    previousValues: List<T>,
    clearPreviousValues: () -> Unit,
    unObserve: suspend () -> Unit,
    override val offerValue: (T) -> Unit
) : IMutableObservableChangedScope<T>,
    ObservableChangedScope<T>(current, previous, previousValues, clearPreviousValues, unObserve)

interface IMutableOwnedObservableChangedScope<O, T> : IOwnedObservableChangedScope<O, T>,
    IMutableObservableChangedScope<T>

open class MutableOwnedObservableChangedScope<O, T>(
    owner: O?,
    current: T,
    previous: T,
    previousValues: List<T>,
    clearPreviousValues: () -> Unit,
    unObserve: suspend () -> Unit,
    override val offerValue: (T) -> Unit
) : IMutableOwnedObservableChangedScope<O, T>,
    OwnedObservableChangedScope<O, T>(owner, current, previous, previousValues, clearPreviousValues, unObserve)