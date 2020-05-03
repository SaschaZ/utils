package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedBase
import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.IOnChangedScope2

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable2<P : Any?, out T : Any?> : IObservableBase<P, T, IOnChangedScope2<P, T>>

/**
 * Same as [IObservable2] but with [Any]? as parent type.Should be used when the parent type is irrelevant.
 */
interface IObservable<out T : Any?> : IObservableBase<Any?, T, IOnChangedScope<T>>

interface IObservableBase<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>> : IOnChangedBase<P, T, S> {

    /**
     * Observe to changes on the internal [value]. Change notification is invoked directly.
     */
    fun observe(listener: S.(@UnsafeVariance T) -> Unit): () -> Unit

    /**
     * Observe to changes on the internal [value]. Change notification is invoked via a coroutine.
     */
    fun observeS(listener: suspend S.(@UnsafeVariance T) -> Unit): () -> Unit
}