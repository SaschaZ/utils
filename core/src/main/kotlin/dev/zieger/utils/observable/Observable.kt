@file:Suppress("FunctionName")

package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.ReadWriteProperty

/**
 * Same as [Observable2] but without a parent type. Use this if you do not care who holds the observed property.
 */
inline fun <T : Any?> Observable(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline onChanged: Observer<T> = {}
): IObservable<T> = object : IObservable<T>,
    IObservableBase<Any?, T, IOnChangedScope<T>> by ObservableBase<Any?, T, IOnChangedScope<T>>(
        initial, onlyNotifyOnChanged, notifyForInitial, storeRecentValues, scope, mutex,
        OnChangedScopeFactory(), { false }, subscriberStateChanged, onChanged
    ) {}

/**
 * [ReadWriteProperty] that can be used to add a read only observer to another property with the delegate pattern.
 *
 * Example:
 * ```kotlin
 *  class Test {Class {
 *      val observable = ObservableDelegate("foo")
 *      private var internalVar by observable
 *  }
 *
 *  fun testFun() {
 *      val testClass = TestClass()
 *      val testClass.observable.observe { }
 *  }
 * ```
 */
inline fun <P : Any?, T : Any?> Observable2(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline onChanged: Observer2<P, T> = {}
): IObservable2<P, T> = object : IObservable2<P, T>,
    IObservableBase<P, T, IOnChangedScope2<P, T>> by ObservableBase<P, T, IOnChangedScope2<P, T>>(
        initial, onlyNotifyOnChanged, notifyForInitial, storeRecentValues, scope, mutex,
        OnChangedScope2Factory(), { false }, subscriberStateChanged, onChanged
    ) {}

inline fun <P : Any?, T : Any?, S : IOnChangedScope2<P, T>> ObservableBase(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    scopeFactory: IScopeFactory<P, T, S>,
    crossinline veto: (T) -> Boolean,
    crossinline subscriberStateChanged: (Boolean) -> Unit = {},
    crossinline onChanged: S.(T) -> Unit,
    base: IOnChangedWritableBase<P, T, S> = OnChangedBase(
        initial, storeRecentValues, notifyForInitial, onlyNotifyOnChanged, scope, mutex, scopeFactory, veto, onChanged
    )
): IObservableWritableBase<P, T, S> =
    object : IObservableWritableBase<P, T, S>, IOnChangedWritableBase<P, T, S> by base {

        private val observer = ArrayList<S.(T) -> Unit>()
        private val observerS = ArrayList<suspend S.(T) -> Unit>()
        private var subscribersAvailable by OnChanged(false) { new ->
            subscriberStateChanged(new)
        }

        override fun observe(listener: S.(T) -> Unit): () -> Unit {
            observer.add(listener)
            if (notifyForInitial)
                createScope(value, null, isInitialNotification = true).listener(value)
            updateSubscriberState()

            return {
                observer.remove(listener)
                updateSubscriberState()
            }
        }

        override fun observeS(listener: suspend S.(T) -> Unit): () -> Unit {
            observerS.add(listener)
            if (notifyForInitial)
                scope?.launchEx(mutex = mutex) {
                    createScope(value, null, isInitialNotification = true).listener(value)
                }
            updateSubscriberState()

            return {
                observerS.remove(listener)
                updateSubscriberState()
            }
        }

        override fun (S).onChangedInternal(value: T) {
            base.run { onChangedInternal(value) }
            ArrayList(observer).forEach { it(value) }
            val scopeToUse = (scope ?: DefaultCoroutineScope())
            ArrayList(observerS).forEach { scopeToUse.launchEx(mutex = mutex) { it(value) } }
        }

        private fun updateSubscriberState() {
            subscribersAvailable = observer.isNotEmpty() || observerS.isNotEmpty()
        }
    }