package de.gapps.utils.observable

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
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
class ObserverDelegate<T>(
    initial: T,
    scope: CoroutineScope =
        DefaultCoroutineScope(Observable::class.name),
    onlyNotifyOnChanged: Boolean = true,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: ChangeObserver<T> = {}
) : ReadWriteProperty<Any, T>, IObservableValue<T> {

    private var internal =
        Observable(initial, scope, onlyNotifyOnChanged, storeRecentValues, subscriberStateChanged, onChanged)

    override fun getValue(thisRef: Any, property: KProperty<*>): T = internal.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        internal.value = value
    }

    override val value: T
        get() = internal.value

    override fun observe(listener: ChangeObserver<T>): () -> Unit = internal.observe(listener)

    override fun observe(channel: SendChannel<T>) = internal.observe(channel)

    override fun clearCache() = internal.clearCache()
}