package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.IOnChangedScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChanged2
import dev.zieger.utils.delegates.OnChangedScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.ReadWriteProperty

typealias Observable<T> = Observable2<Any?, T>

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
    private val notifyForExistingInternal: Boolean = false,
    storeRecentValues: Boolean = false,
    scope: CoroutineScope? = null,
    mutex: Mutex? = null,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: IOnChangedScope<P, T>.(T) -> Unit = {}
) : IObservable2<P, T>,
    OnChanged2<P, T>(
        initial, storeRecentValues, false, onlyNotifyOnChanged, scope, mutex,
        onChange = onChanged
    ) {

    private val observer = ArrayList<Observer2<@UnsafeVariance P, @UnsafeVariance T>>()
    private val observerS = ArrayList<Observer2S<@UnsafeVariance P, @UnsafeVariance T>>()
    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }

    override fun observe(listener: Observer2<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit {
        observer.add(listener)
        if (notifyForExistingInternal)
            OnChangedScope<P, T>(value, null, null, emptyList(), { clearRecentValues() })
                .listener(value)
        updateSubscriberState()

        return {
            observer.remove(listener)
            updateSubscriberState()
        }
    }

    override fun observeS(listener: Observer2S<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit {
        observerS.add(listener)
        if (notifyForExistingInternal)
            scope?.launchEx(mutex = mutex) {
                OnChangedScope<P, T>(value, null, null, emptyList(), { clearRecentValues() })
                    .listener(value)
            }
        updateSubscriberState()

        return {
            observerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun IOnChangedScope<P, @UnsafeVariance T>.onChanged(value: @UnsafeVariance T) =
        observer.forEach { it(value) }

    override suspend fun IOnChangedScope<P, @UnsafeVariance T>.onChangedS(value: @UnsafeVariance T) =
        observerS.forEach { it(value) }

    private fun updateSubscriberState() {
        subscribersAvailable = observer.isNotEmpty() || observerS.isNotEmpty()
    }
}