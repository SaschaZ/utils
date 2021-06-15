package dev.zieger.utils.observables

open class ObservableChangedScope<T>(
    open val current: T,
    open val previous: T,
    open val previousValues: List<T>,
    open val clearPreviousValues: () -> Unit,
    open var unObserve: suspend () -> Unit
)