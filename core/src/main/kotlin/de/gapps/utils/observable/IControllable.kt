package de.gapps.utils.observable

import kotlin.properties.ReadWriteProperty

/**
 * Same as [IObservable] but allows to change the internal variable.
 */
interface IControllable<out T> : ReadWriteProperty<Any, @UnsafeVariance T> {

    /**
     * Controlled variable.
     * Changes on this variable will notify registered observers immediately.
     */
    var value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun control(listener: ControlObserver<T>): () -> Unit
}