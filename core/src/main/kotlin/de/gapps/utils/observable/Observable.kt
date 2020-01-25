package de.gapps.utils.observable

import de.gapps.utils.delegates.IOnChangedScope
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * [ReadWriteProperty] that can be used to add a read only observer to another property with the delegate pattern.
 *
 * Example:
 * ```
 *  class TestClass {
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
class Observable<out P : Any?, out T : Any?>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: IOnChangedScope<P, T>.(T) -> Unit = {}
) : ReadWriteProperty<@UnsafeVariance P, @UnsafeVariance T>, IObservable<P, T> {

    private var internal =
        Controllable(
            initial, onlyNotifyOnChanged, notifyForExisting, storeRecentValues, subscriberStateChanged,
            onChanged
        )

    override fun getValue(thisRef: @UnsafeVariance P, property: KProperty<*>): T = internal.value

    override fun setValue(thisRef: @UnsafeVariance P, property: KProperty<*>, value: @UnsafeVariance T) {
        internal.value = value
    }

    override val value: T
        get() = internal.value

    override fun observe(listener: IOnChangedScope<@UnsafeVariance P, T>.(T) -> Unit): () -> Unit =
        internal.control(listener)

    override fun clearRecentValues() = internal.clearRecentValues()
}