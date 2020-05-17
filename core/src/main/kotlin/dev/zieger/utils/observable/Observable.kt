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
open class Observable<out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override var notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: Observer<T> = {}
) : IObservable<T>, ObservableBase<Any?, T, IOnChangedScope<T>>(
    initial, onlyNotifyOnChanged, notifyForInitial, storeRecentValues, scope, mutex,
    subscriberStateChanged, onChanged, OnChangedScopeFactory()
)

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
open class Observable2<P : Any?, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override var notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: Observer2<P, T>? = null
) : IObservable2<P, T>, ObservableBase<P, T, IOnChangedScope2<P, T>>(
    initial, onlyNotifyOnChanged, notifyForInitial, storeRecentValues, scope, mutex, subscriberStateChanged, onChanged,
    OnChangedScope2Factory()
)

abstract class ObservableBase<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    override var notifyForInitial: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: (S.(T) -> Unit)? = null,
    scopeFactory: IScope2Factory<P, T, S>
) : IObservableBase<P, T, S>,
    OnChangedBase<P, T, S>(
        initial, storeRecentValues, notifyForInitial, onlyNotifyOnChanged, scope, mutex, scopeFactory, { false }, {}, {}
    ) {

    private val observer = ArrayList<S.(T) -> Unit>()
    private val observerS = ArrayList<suspend S.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    init {
        onChanged?.also {
            (scope ?: DefaultCoroutineScope()).launchEx {
                observe(onChanged)
            }
        }
    }

    override fun observe(listener: S.(T) -> Unit): () -> Unit {
        observer.add(listener)
        if (notifyForInitial) createScope(value, previousThisRef.get(), recentValues.lastOrNull(), recentValues,
            { recentValues.clear() }, true
        ).listener(value)
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
                createScope(value, previousThisRef.get(), recentValues.lastOrNull(), recentValues,
                    { recentValues.clear() }, true
                ).listener(value)
            }
        updateSubscriberState()

        return {
            observerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun (@UnsafeVariance S).onChangedInternal(value: @UnsafeVariance T) =
        ArrayList(observer).forEach { it(value) }

    override suspend fun (@UnsafeVariance S).onChangedSInternal(value: @UnsafeVariance T) =
        ArrayList(observerS).forEach { it(value) }

    private fun updateSubscriberState() {
        subscribersAvailable = observer.isNotEmpty() || observerS.isNotEmpty()
    }
}