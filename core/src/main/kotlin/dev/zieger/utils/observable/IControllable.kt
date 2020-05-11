@file:Suppress("unused")

package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedBase

interface IControllable<out T : Any?> : IControllableBase<Any?, @UnsafeVariance T, IControlledChangedScope<T>>

/**
 * Same as [IObservable] but allows to change the internal variable.
 */
interface IControllable2<P : Any?, out T : Any?> :
    IControllableBase<P, @UnsafeVariance T, IControlledChangedScope2<P, T>>

interface IControllableBase<P : Any?, out T : Any?, out S : IControlledChangedScope2<P, T>> : IOnChangedBase<P, T, S> {

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun control(listener: S.(T) -> Unit): () -> Unit

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun controlS(listener: suspend S.(T) -> Unit): () -> Unit
}