@file:Suppress("unused")

package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.IOnChangedScopeWithParent
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChangedWithParent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.ReadWriteProperty


typealias Observable<T> = ObservableWithParent<Any?, T>

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
open class ObservableWithParent<P : Any?, T : Any?>(
    params: IObservableParamsWithParent<P, T>
) : IObservableWithParent<P, T>,
    OnChangedWithParent<P, T>(params) {

    constructor(
        initial: T,
        scope: CoroutineScope? = null,
        storeRecentValues: Boolean = false,
        previousValueSize: Int = if (storeRecentValues) 100 else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        mutex: Mutex = Mutex(),
        safeSet: Boolean = false,
        subscriberStateChanged: ((Boolean) -> Unit)? = {},
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChangedS: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {},
        onChanged: IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
    ) : this(
        ObservableParamsWithParent(
            initial, scope, storeRecentValues, previousValueSize, notifyForInitial, notifyOnChangedValueOnly, mutex,
            safeSet, subscriberStateChanged, veto, map, onChangedS, onChanged
        )
    )

    private val observer = ArrayList<IOnChangedScopeWithParent<P, T>.(T) -> Unit>()
    private val observerS = ArrayList<suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        params.onSubscriberStateChanged?.invoke(new)
    }

    init {
        @Suppress("LeakingThis")
        params.onChanged?.also { observe(it) }
    }

    override fun observe(listener: IOnChangedScopeWithParent<P, T>.(T) -> Unit): () -> Unit {
        observer.add(listener)
        if (notifyForInitial)
            buildOnChangedScope(null, true).listener(value)
        updateSubscriberState()

        return {
            observer.remove(listener)
            updateSubscriberState()
        }
    }

    override fun observeS(listener: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit): () -> Unit {
        observerS.add(listener)
        if (notifyForInitial)
            scope?.launchEx(mutex = mutex) {
                buildOnChangedScope(null, true).listener(value)
            }
        updateSubscriberState()

        return {
            observerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun IOnChangedScopeWithParent<P, T>.onChangedInternal(value: T) =
        ArrayList(observer).forEach { it(value) }

    override suspend fun IOnChangedScopeWithParent<P, T>.onChangedSInternal(value: T) =
        ArrayList(observerS).forEach { it(value) }

    private fun updateSubscriberState() {
        subscribersAvailable = observer.isNotEmpty() || observerS.isNotEmpty()
    }
}