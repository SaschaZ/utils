package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChanged2

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable2<out P : Any?, out T : Any?> : IOnChanged2<@UnsafeVariance P, @UnsafeVariance T> {

    /**
     * Observe to changes on the internal [value]. Change notification is invoked directly.
     */
    fun observe(listener: Observer2<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit

    /**
     * Observe to changes on the internal [value]. Change notification is invoked via a coroutine.
     */
    fun observeS(listener: Observer2S<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit
}

/**
 * Same as [IObservable2] but with [Any]? as parent type.Should be used when the parent type is irrelevant.
 */
interface IObservable<out T> : IObservable2<Any?, T>