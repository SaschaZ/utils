package dev.zieger.utils.observable

import kotlin.properties.ReadWriteProperty

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable2<out P : Any?, out T : Any?> : ReadWriteProperty<@UnsafeVariance P, @UnsafeVariance T> {

    /**
     * Observed variable.
     * Changes on this variable will notify registered observers.
     * Depending on the implementation changes on this variable are not instantly applied.
     */
    val value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value]. Change is notified immediately.
     */
    fun observe(listener: Observer2<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit

    fun clearRecentValues()
}

/**
 * Same as [IObservable2] but with [Any]? as parent type.Should be used when the parent type is irrelevant.
 */
interface IObservable<T> : IObservable2<Any?, T>