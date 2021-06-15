package dev.zieger.utils.observables

open class MutableObservableChangedScope<T>(
    current: T,
    previous: T,
    previousValues: List<T>,
    clearPreviousValues: () -> Unit,
    unObserve: suspend () -> Unit,
    protected open val set: (T) -> Unit
) : ObservableChangedScope<T>(current, previous, previousValues, clearPreviousValues, unObserve) {

    override var current: T = current
        set(value) {
            field = value
            set(value)
        }
}