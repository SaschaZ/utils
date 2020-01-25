package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChangedScope
import kotlin.properties.ReadWriteProperty

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable2<out P: Any?, out T: Any?> : ReadWriteProperty<@UnsafeVariance P, @UnsafeVariance T> {

    /**
     * Observed variable.
     * Changes on this variable will notify registered observers.
     * Depending on the implementation changes on this variable are not instantly applied.
     */
    val value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value]. Change is notified immediately.
     */
    fun observe(listener: Observer2<@UnsafeVariance P, @UnsafeVariance T>): ()-> Unit

    fun clearRecentValues()
}

typealias IObservable<T> = IObservable2<Any?, T>

typealias Observer<T> = IOnChangedScope<Any?, T>.(T) -> Unit
typealias Observer2<P, T> = IOnChangedScope<P, T>.(T) -> Unit