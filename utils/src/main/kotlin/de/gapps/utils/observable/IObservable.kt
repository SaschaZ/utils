package de.gapps.utils.observable

import kotlinx.coroutines.channels.SendChannel

interface IObservableValue<out T> {

    /**
     * Observed variable.
     * Changes on this variable will notify registered observers.
     * Depending on the implementation changes on this variable are not instantly applied.
     */
    val value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value]. Change is notified immediately.
     */
    fun observe(listener: ChangeObserver<T>): () -> Unit

    /**
     * Observe to changes on the internal [value]. Change is notified asynchronously.
     */
    fun observe(channel: SendChannel<T>)

    fun clearCache()
}

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable<out T> : IObservableValue<T> {

    /**
     * Observed variable.
     * Changes on this variable will notify registered observers.
     * Depending on the implementation changes on this variable are not instantly applied.
     */
    override var value: @UnsafeVariance T
}