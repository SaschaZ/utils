package de.gapps.utils.observable

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.name
import kotlinx.coroutines.CoroutineScope
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
class Observable<T> private constructor(
    initial: T,
    scope: CoroutineScope =
        DefaultCoroutineScope(Controllable::class.name),
    onlyNotifyOnChanged: Boolean = true,
    storeRecentValues: Boolean = false,
    subscriberStateChanged: ((Boolean) -> Unit)? = null,
    cacheHolder: CachingValueHolder<T>,
    onChanged: ChangeObserver<T> = {}
) : ICachingValueHolder<T> by cacheHolder, IObservable<T> {

    constructor(
        initial: T,
        scope: CoroutineScope =
            DefaultCoroutineScope(Controllable::class.name),
        onlyNotifyOnChanged: Boolean = true,
        storeRecentValues: Boolean = false,
        subscriberStateChanged: ((Boolean) -> Unit)? = null,
        onChanged: ChangeObserver<T> = {}
    ) :
            this(
                initial, scope, onlyNotifyOnChanged, storeRecentValues, subscriberStateChanged,
                CachingValueHolder(initial), onChanged
            )

    private var internal =
        Controllable(initial, scope, onlyNotifyOnChanged, storeRecentValues, subscriberStateChanged, onChanged)

    override fun getValue(thisRef: Any, property: KProperty<*>): T = internal.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        internal.value = value
    }

    override val value: T
        get() = internal.value

    override fun observe(listener: ChangeObserver<T>): () -> Unit = internal.control(listener)

    override fun clearCache() = internal.clearCache()
}