@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface IObservableBase<T> : ReadOnlyProperty<Any?, T> {

    val value: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    fun clearPreviousValues()

    suspend fun suspendUntilNext(timeout: ITimeSpan? = null): T =
        suspendUntil(timeout) { true }

    suspend fun suspendUntil(wanted: T, timeout: ITimeSpan? = null): T =
        suspendUntil(timeout) { it == wanted }

    suspend fun suspendUntil(timeout: ITimeSpan? = null, wanted: suspend (T) -> Boolean): T

    suspend fun suspendUntilValid(timeout: ITimeSpan? = null): Boolean = throw NotImplementedError()

    suspend fun release()
}

interface IObservable<T> : IObservableBase<T> {

    suspend fun observe(
        changesOnly: Boolean = true,
        listener: Observer<T, ObservableChangedScope<T>>
    ): suspend () -> Unit
}

open class Observable<T> internal constructor(
    private val mutableObservable: IMutableObservable<T>
) : IObservable<T>, IObservableBase<T> by mutableObservable {

    constructor(
        initial: T,
        dispatcher: CoroutineContext = ObservableDispatcherHolder.context,
        observer: MutableObserver<T, ObservableChangedScope<T>>? = null
    ) :
            this(MutableObservable(initial, dispatcher, observer))

    override suspend fun observe(
        changesOnly: Boolean,
        listener: Observer<T, ObservableChangedScope<T>>
    ): suspend () -> Unit = mutableObservable.observe { listener(it) }
}

typealias Observer<T, S> = suspend S.(current: T) -> Unit

typealias MutableObserver<T, S> = suspend S.(current: T) -> Unit



