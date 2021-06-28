@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface IObservableBase<O, T> : ReadOnlyProperty<O, T> {

    val value: T
    var owner: O?

    override fun getValue(thisRef: O, property: KProperty<*>): T {
        owner = thisRef
        return value
    }

    fun clearPreviousValues()

    suspend fun suspendUntilNext(timeout: ITimeSpan? = null): T =
        suspendUntil(timeout) { true }

    suspend fun suspendUntil(wanted: T, timeout: ITimeSpan? = null): T =
        suspendUntil(timeout) { it == wanted }

    suspend fun suspendUntil(timeout: ITimeSpan? = null, wanted: suspend (T) -> Boolean): T

    suspend fun release()
}

interface IObservable<T> : IObservableBase<Any?, T> {

    suspend fun observe(
        changesOnly: Boolean = true,
        notifyForInitial: Boolean = false,
        listener: Observer<T, IObservableChangedScope<T>>
    ): suspend () -> Unit
}

interface IOwnedObservable<O, T> : IObservableBase<O, T> {

    suspend fun observe(
        changesOnly: Boolean = true,
        notifyForInitial: Boolean = false,
        listener: Observer<T, IOwnedObservableChangedScope<O, T>>
    ): suspend () -> Unit
}

open class Observable<T> internal constructor(
    private val mutableObservable: IMutableObservable<T>
) : IObservable<T>, IObservableBase<Any?, T> by mutableObservable {

    constructor(
        initial: T,
        changesOnly: Boolean = true,
        notifyForInitial: Boolean = false,
        dispatcher: CoroutineContext = ObservableDispatcherHolder.context,
        observer: MutableObserver<T, IObservableChangedScope<T>>? = null
    ) : this(MutableObservable(initial, changesOnly, notifyForInitial, dispatcher, initialObserver = observer))

    override suspend fun observe(
        changesOnly: Boolean,
        notifyForInitial: Boolean,
        listener: Observer<T, IObservableChangedScope<T>>
    ): suspend () -> Unit = mutableObservable.observe(changesOnly, notifyForInitial) { listener(it) }
}

open class OwnedObservable<O, T> internal constructor(
    private val mutableObservable: IMutableOwnedObservable<O, T>
) : IOwnedObservable<O, T>, IObservableBase<O, T> by mutableObservable {

    constructor(
        initial: T,
        changesOnly: Boolean = true,
        notifyForInitial: Boolean = false,
        dispatcher: CoroutineContext = ObservableDispatcherHolder.context,
        observer: MutableObserver<T, IObservableChangedScope<T>>? = null
    ) : this(MutableOwnedObservable(initial, changesOnly, notifyForInitial, dispatcher, initialObserver = observer))

    override suspend fun observe(
        changesOnly: Boolean,
        notifyForInitial: Boolean,
        listener: Observer<T, IOwnedObservableChangedScope<O, T>>
    ): suspend () -> Unit = mutableObservable.observe(changesOnly, notifyForInitial) { listener(it) }
}

typealias Observer<T, S> = suspend S.(current: T) -> Unit

typealias MutableObserver<T, S> = suspend S.(current: T) -> Unit



