package de.gapps.utils.observable

import de.gapps.utils.delegates.OnChanged
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
class Observable<T>(
    initial: T,
    onlyNotifyOnChanged: Boolean = true,
    notifyForExisting: Boolean = false,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: ChangeObserver<T> = {}
) : ReadWriteProperty<Any, T>, IObservable<T> {

    private var internal =
        Controllable(
            initial, onlyNotifyOnChanged, notifyForExisting, storeRecentValues, subscriberStateChanged,
            onChanged
        )

    override fun getValue(thisRef: Any, property: KProperty<*>): T = internal.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        internal.value = value
    }

    override val value: T
        get() = internal.value

    override fun observe(listener: ChangeObserver<T>): () -> Unit = internal.control(listener)
}