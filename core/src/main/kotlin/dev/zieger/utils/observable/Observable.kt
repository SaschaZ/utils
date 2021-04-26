@file:Suppress("unused", "LeakingThis")

package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.IOnChangedScopeWithParent
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.delegates.OnChangedWithParent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
        buildScope: () -> CoroutineScope,
        storeRecentValues: Boolean = false,
        previousValueSize: Int = if (storeRecentValues) 100 else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        mutex: Mutex = Mutex(),
        subscriberStateChanged: ((Int) -> Unit)? = {},
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChanged: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
    ) : this(
        ObservableParamsWithParent(
            initial,
            buildScope,
            storeRecentValues,
            previousValueSize,
            notifyForInitial,
            notifyOnChangedValueOnly,
            mutex,
            subscriberStateChanged,
            veto,
            map,
            onChanged
        )
    )

    constructor(
        initial: T,
        scope: CoroutineScope,
        storeRecentValues: Boolean = false,
        previousValueSize: Int = if (storeRecentValues) 100 else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        mutex: Mutex = Mutex(),
        subscriberStateChanged: ((Int) -> Unit)? = {},
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChanged: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
    ) : this(
        ObservableParamsWithParent(
            initial, scope, storeRecentValues, previousValueSize, notifyForInitial, notifyOnChangedValueOnly, mutex,
            subscriberStateChanged, veto, map, onChanged
        )
    )

    private val mutableStateFlow = MutableStateFlow(initial)
    private val subscriberAvailableStateFlow = MutableStateFlow(0)

    private val observer = ArrayList<suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(0) { new ->
        subscriberAvailableStateFlow.value = new
    }

    init {
        @Suppress("LeakingThis")
        params.onChangedS?.also { oc -> observe { oc(it) } }
    }

    override fun observe(
        scope: CoroutineScope,
        listener: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit
    ): () -> Unit {
        val job = scope.launchEx(useSuperVisorJob = true, printStackTrace = false) {
            mutableStateFlow.collect {
                buildOnChangedScope(previousValues.lastOrNull()).listener(value)
            }
            if (notifyForInitial)
                buildOnChangedScope(null, true).listener(value)
        }
        updateSubscriberState()

        return {
            job.cancel()
            observer.remove(listener)
            updateSubscriberState()
        }
    }

    override fun IOnChangedScopeWithParent<P, T>.onChangedInternal(value: T) {
        mutableStateFlow.value = value
    }

    override suspend fun IOnChangedScopeWithParent<P, T>.onChangedSInternal(value: T) {
        ArrayList(observer).forEach { it(value) }
    }

    private fun updateSubscriberState() {
        subscribersAvailable = observer.size
    }

    open fun release() {
        observer.clear()
    }
}