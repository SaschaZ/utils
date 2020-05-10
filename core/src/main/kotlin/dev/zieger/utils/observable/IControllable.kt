@file:Suppress("unused")

package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IScopeFactory

interface IControllable<out T : Any?> : IControllableBase<Any?, @UnsafeVariance T, IControlledChangedScope<T>>

/**
 * Same as [IObservable] but allows to change the internal variable.
 */
interface IControllable2<P, out T> : IControllableBase<P, @UnsafeVariance T, IControlledChangedScope2<P, T>>

interface IControllableBase<P : Any?, out T : Any?, out S : IControlledChangedScope2<P, T>> :
    IObservableWritableBase<P, T, S> {

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun control(listener: S.(T) -> Unit): () -> Unit

    /**
     * Observe to changes on the internal [value] and change internal value if needed.
     */
    fun controlS(listener: suspend S.(T) -> Unit): () -> Unit
}

class ControlledChangedScopeFactory<out T : Any?> : IScopeFactory<Any?, T, IControlledChangedScope<T>> {
    override fun createScope(
        newValue: @UnsafeVariance T, thisRef: Any?, previousValue: @UnsafeVariance T?,
        recentValues: List<@UnsafeVariance T?>, clearRecentValues: () -> Unit,
        isInitialNotification: Boolean, setter: (T) -> Unit
    ): IControlledChangedScope<T> =
        ControlledChangedScope(
            newValue,
            thisRef,
            previousValue,
            recentValues,
            clearRecentValues,
            setter,
            isInitialNotification
        )
}

class ControlledChangedScope2Factory<P : Any?, out T : Any?> : IScopeFactory<P, T, IControlledChangedScope2<P, T>> {
    override fun createScope(
        newValue: @UnsafeVariance T, thisRef: P?, previousValue: @UnsafeVariance T?,
        recentValues: List<@UnsafeVariance T?>, clearRecentValues: () -> Unit,
        isInitialNotification: Boolean, setter: (T) -> Unit
    ): IControlledChangedScope2<P, T> =
        ControlledChangedScope2(
            newValue,
            thisRef,
            previousValue,
            recentValues,
            clearRecentValues,
            setter,
            isInitialNotification
        )
}