package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChangedScope
import kotlin.properties.ReadWriteProperty

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable<out P : Any?, out T> : ReadWriteProperty<@UnsafeVariance P, @UnsafeVariance T> {

    /**
     * Observed variable.
     * Changes on this variable will notify registered observers.
     * Depending on the implementation changes on this variable are not instantly applied.
     */
    val value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value]. Change is notified immediately.
     */
    fun observe(listener: IOnChangedScope<@UnsafeVariance P, T>.(T) -> Unit): () -> Unit

    fun clearRecentValues()
}

