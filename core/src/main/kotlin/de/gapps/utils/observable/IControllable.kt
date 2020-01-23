package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChanged

/**
 * Same as [IObservable] but allows to change the internal variable.
 */
interface IControllable<P : Any, out T> : IOnChanged<P, @UnsafeVariance T> {

    /**
     * Controlled variable.
     * Changes on this variable will notify registered observers immediately.
     */
    override var value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun control(listener: IControlledChangedScope<P, T>.(T) -> Unit): () -> Unit
}