package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.*
import dev.zieger.utils.misc.DataClass
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

    constructor(initial: T, onChanged: IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}) :
            this(ObservableParamsWithParent(initial, onChanged = onChanged))

    private val observer = ArrayList<IOnChangedScopeWithParent<P, T>.(T) -> Unit>()
    private val observerS = ArrayList<suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        params.subscriberStateChanged?.invoke(new)
    }

    init {
        @Suppress("LeakingThis")
        observe(params.onChanged)
    }

    override fun observe(listener: IOnChangedScopeWithParent<P, T>.(T) -> Unit): () -> Unit {
        observer.add(listener)
        if (notifyForInitial) OnChangedScopeWithParent(
            value, previousThisRef.get(), previousValues.lastOrNull(), previousValues,
            { previousValues.clear() }, true
        ).listener(value)
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
                OnChangedScopeWithParent(
                    value, previousThisRef.get(), previousValues.lastOrNull(), previousValues,
                    { previousValues.clear() }, true
                ).listener(value)
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

interface IObservableParamsWithParent<P : Any?, T : Any?> : IOnChangedParamsWithParent<P, T> {
    val subscriberStateChanged: ((Boolean) -> Unit)?
}

class ObservableParamsWithParent<P : Any?, T : Any?>(
    override val initial: T,
    storeRecentValues: Boolean = false,
    override val previousValueSize: Int = if (storeRecentValues) 100 else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val mutex: Mutex = Mutex(),
    override val safeSet: Boolean = false,
    override val subscriberStateChanged: ((Boolean) -> Unit)? = {},
    override val veto: (T) -> Boolean = { false },
    override val map: (T) -> T = { it },
    override val onChangedS: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {},
    override val onChanged: IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
) : DataClass(), IObservableParamsWithParent<P, T>

typealias ObservableParams<T> = ObservableParamsWithParent<Any?, T>