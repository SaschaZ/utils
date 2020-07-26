package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChanged2
import dev.zieger.utils.delegates.IOnChangedScope2

/**
 * Same as [IObservable2] but with [Any]? as parent type.Should be used when the parent type is irrelevant.
 */
typealias IObservable<T> = IObservable2<Any?, T>

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservable2<P : Any?, T : Any?> : IOnChanged2<P, T> {

    /**
     * Observe to changes on the internal [value]. Change notification is invoked directly.
     */
    fun observe(listener: IOnChangedScope2<P, T>.(T) -> Unit): () -> Unit

    /**
     * Observe to changes on the internal [value]. Change notification is invoked via a coroutine.
     */
    fun observeS(listener: suspend IOnChangedScope2<P, T>.(T) -> Unit): () -> Unit
}