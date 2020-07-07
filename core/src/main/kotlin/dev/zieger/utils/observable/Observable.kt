package dev.zieger.utils.observable

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.*
import dev.zieger.utils.misc.DataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.properties.ReadWriteProperty

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
open class Observable2<P : Any?, T : Any?>(
    params: IObservableParams2<P, T>
) : IObservable2<P, T>, ObservableBase<P, T>(params) {
    constructor(initial: T, onChanged: IOnChangedScope2<P, T>.(T) -> Unit = {}) :
            this(ObservableParams2(initial, onChanged = onChanged))
}

typealias Observable<T> = Observable2<Any?, T>

abstract class ObservableBase<P : Any?, T : Any?>(
    params: IObservableParams2<P, T>
) : IObservable2<P, T>,
    OnChanged2<P, T>(params) {

    private val observer = ArrayList<IOnChangedScope2<P, T>.(T) -> Unit>()
    private val observerS = ArrayList<suspend IOnChangedScope2<P, T>.(T) -> Unit>()
    private var subscribersAvailable by OnChanged(false) { new ->
        params.subscriberStateChanged?.invoke(new)
    }

    init {
        @Suppress("LeakingThis")
        observe(params.onChanged)
    }

    override fun observe(listener: IOnChangedScope2<P, T>.(T) -> Unit): () -> Unit {
        observer.add(listener)
        if (notifyForInitial) OnChangedScope2(
            value, previousThisRef.get(), recentValues.lastOrNull(), recentValues,
            { recentValues.reset() }, true
        ).listener(value)
        updateSubscriberState()

        return {
            observer.remove(listener)
            updateSubscriberState()
        }
    }

    override fun observeS(listener: suspend IOnChangedScope2<P, T>.(T) -> Unit): () -> Unit {
        observerS.add(listener)
        if (notifyForInitial)
            scope?.launchEx(mutex = mutex) {
                OnChangedScope2(
                    value, previousThisRef.get(), recentValues.lastOrNull(), recentValues,
                    { recentValues.reset() }, true
                ).listener(value)
            }
        updateSubscriberState()

        return {
            observerS.remove(listener)
            updateSubscriberState()
        }
    }

    override fun IOnChangedScope2<P, T>.onChangedInternal(value: T) =
        ArrayList(observer).forEach { it(value) }

    override suspend fun IOnChangedScope2<P, T>.onChangedSInternal(value: T) =
        ArrayList(observerS).forEach { it(value) }

    private fun updateSubscriberState() {
        subscribersAvailable = observer.isNotEmpty() || observerS.isNotEmpty()
    }
}

interface IObservableParams2<P : Any?, T : Any?> : IOnChangedParams2<P, T> {
    val subscriberStateChanged: ((Boolean) -> Unit)?
}

class ObservableParams2<P : Any?, T : Any?>(
    override val initial: T,
    storeRecentValues: Boolean = false,
    override val recentValueSize: Int = if (storeRecentValues) 100 else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val scope: CoroutineScope? = null,
    override val mutex: Mutex? = null,
    override val subscriberStateChanged: ((Boolean) -> Unit)? = {},
    override val veto: (T) -> Boolean = { false },
    override val onChangedS: suspend IOnChangedScope2<P, T>.(T) -> Unit = {},
    override val onChanged: IOnChangedScope2<P, T>.(T) -> Unit = {}
) : DataClass(), IObservableParams2<P, T>

typealias ObservableParams<T> = ObservableParams2<Any?, T>