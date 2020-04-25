package dev.zieger.utils.observable

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Observable<out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: Observer<T> = {}
) : IObservable<@UnsafeVariance T>, Observable2<Any?, T>(
    initial, onlyNotifyOnChanged, notifyForExisting, storeRecentValues, subscriberStateChanged,
    onChanged
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
open class Observable2<out P : Any?, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: Observer2<P, T> = {}
) : ReadWriteProperty<@UnsafeVariance P, @UnsafeVariance T>, IObservable2<P, T> {

    private var internal =
        Controllable2(
            initial, onlyNotifyOnChanged, notifyForExisting, storeRecentValues, subscriberStateChanged,
            onChanged
        )

    override fun getValue(thisRef: @UnsafeVariance P, property: KProperty<*>): T = internal.value

    override fun setValue(thisRef: @UnsafeVariance P, property: KProperty<*>, value: @UnsafeVariance T) {
        internal.value = value
    }

    override val value: T
        get() = internal.value

    override fun observe(listener: Observer2<@UnsafeVariance P, @UnsafeVariance T>): () -> Unit =
        internal.control(listener)

    override fun clearRecentValues() = internal.clearRecentValues()
}