package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface Observable<T> : ReadWriteProperty<Any?, T> {

    var value: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    fun clearPreviousValues() = Unit

    suspend fun changeValue(block: suspend ObservableChangedScope<T>.(current: T) -> T)

    suspend fun suspendUntilNext(timeout: ITimeSpan? = null, remove: Boolean = true): T =
        suspendUntil({ true }, timeout, remove)
    suspend fun suspendUntil(wanted: T, timeout: ITimeSpan? = null, remove: Boolean = true): T =
        suspendUntil({ it == wanted }, timeout, remove)
    suspend fun suspendUntil(wanted: suspend (T) -> Boolean, timeout: ITimeSpan? = null, remove: Boolean = true): T

    suspend fun observe(
        changesOnly: Boolean = true,
        listener: Observer<T>
    ): suspend () -> Unit
}

open class SimpleObservable<T>(
    initial: T,
    private val context: CoroutineContext = newSingleThreadContext("")
) : Observable<T> {

    private val valueMutex = Mutex()

    private var internalValue: T = initial

    override var value: T
        get() = internalValue
        set(value) = runBlocking(context) {
            val (old, new) = valueMutex.withLock {
                val old = internalValue
                internalValue = value
                old to internalValue
            }
            onPropertyChanged(old, new)
        }

    private val observerMutex = Mutex()
    private val observer = LinkedList<Observer<T>>()

    private val valueWaiterMutex = Mutex()
    private val valueWaiter = HashMap<suspend (T) -> Boolean, suspend (T) -> Boolean>()

    private val onChangedChannel = Channel<Pair<T, T>>(Channel.RENDEZVOUS)
    private val job: Job = CoroutineScope(context).launch {
        for ((old, new) in onChangedChannel) {
            observerMutex.withLock {
                ObservableChangedScope(this@SimpleObservable, old).run {
                    observer.forEach { launch(context) { it(new) } }
                }
            }
            valueWaiterMutex.withLock {
                valueWaiter.filter { (key, value) -> key(new) && value(new) }
                    .onEach { (key, _) -> valueWaiter.remove(key) }
            }
        }
    }

    private suspend fun onPropertyChanged(old: T, new: T) = onChangedChannel.send(old to new)

    override suspend fun changeValue(block: suspend ObservableChangedScope<T>.(current: T) -> T) {
        val (old, new) = valueMutex.withLock {
            val old = internalValue
            internalValue = ObservableChangedScope(this@SimpleObservable, old).block(value)
            old to internalValue
        }
        onPropertyChanged(old, new)
    }

    override suspend fun suspendUntil(wanted: suspend (T) -> Boolean, timeout: ITimeSpan?, remove: Boolean): T {
        val result = Channel<T>()
        valueWaiterMutex.withLock {
            valueWaiter[wanted] = {
                result.send(it)
                remove
            }
        }
        return result.receive()
    }

    override suspend fun observe(
        changesOnly: Boolean,
        listener: Observer<T>
    ): suspend () -> Unit {
        val l: Observer<T> = {
            if (previous != current || !changesOnly)
                listener(current)
        }
        observerMutex.withLock {
            observer.add(l)
        }
        return { observerMutex.withLock { observer.remove(l) } }
    }

    suspend fun release() {
        job.cancelAndJoin()
    }
}

open class FlowObservable<T>(initial: T) : Observable<T> {

    override var value: T
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun changeValue(block: suspend ObservableChangedScope<T>.(current: T) -> T) {
        TODO("Not yet implemented")
    }

    override suspend fun suspendUntil(wanted: suspend (T) -> Boolean, timeout: ITimeSpan?, remove: Boolean): T {
        TODO("Not yet implemented")
    }

    override suspend fun observe(changesOnly: Boolean, listener: Observer<T>): suspend () -> Unit {
        TODO("Not yet implemented")
    }

}

typealias Observer<T> = suspend ObservableChangedScope<T>.(current: T) -> Unit

data class ObservableChangedScope<T>(
    val current: T,
    val previous: T,
    val previousValues: List<T>,
    val clearPreviousValues: () -> Unit
) {
    constructor(
        observable: Observable<T>,
        previous: T
    ) : this(observable.value, previous, emptyList(), { observable.clearPreviousValues() })
}