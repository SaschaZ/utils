package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChanged2

/**
 * Same as [IObservable] but allows to change the internal variable.
 */
interface IControllable2<out P, out T> : IOnChanged2<@UnsafeVariance P, @UnsafeVariance T> {

    /**
     * Controlled variable.
     * Changes on this variable will notify registered observers immediately.
     */
    override var value: @UnsafeVariance T

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun control(listener: Controller2<P, T>): () -> Unit

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun controlS(listener: Controller2S<P, T>): () -> Unit
}

interface IControllable<out T> : IControllable2<Any?, T> {

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    override fun control(listener: Controller<T>): () -> Unit

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    override fun controlS(listener: ControllerS<T>): () -> Unit
}