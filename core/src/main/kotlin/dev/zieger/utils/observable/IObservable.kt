package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedScopeWithParent
import dev.zieger.utils.delegates.IOnChangedWithParent

/**
 * Same as [IObservableWithParent] but with [Any]? as parent type.Should be used when the parent type is irrelevant.
 */
typealias IObservable<T> = IObservableWithParent<Any?, T>

/**
 * Describes a container that holds a variable of type [T] and provides methods to observe changes on this variable.
 */
interface IObservableWithParent<P : Any?, T : Any?> : IOnChangedWithParent<P, T> {

    /**
     * Observe to changes on the internal [value]. Change notification is invoked directly.
     */
    fun observe(listener: IOnChangedScopeWithParent<P, T>.(T) -> Unit): () -> Unit

    /**
     * Observe to changes on the internal [value]. Change notification is invoked via a coroutine.
     */
    fun observeS(listener: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit): () -> Unit
}