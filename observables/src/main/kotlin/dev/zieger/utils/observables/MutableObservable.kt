@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface IMutableObservable<T> : IObservableBase<T>, ReadWriteProperty<Any?, T> {

    override var value: T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    suspend fun changeValue(block: suspend MutableObservableChangedScope<T>.(current: T) -> T)

    suspend fun observe(
        changesOnly: Boolean = true,
        scope: CoroutineScope? = null,
        listener: MutableObserver<T, MutableObservableChangedScope<T>>
    ): suspend () -> Unit

    fun toImmutableObservable(): IObservable<T>
}

open class MutableObservable<T>(
    private val initial: T,
    dispatcher: CoroutineContext = ObservableDispatcherHolder.context,
    observer: MutableObserver<T, ObservableChangedScope<T>>? = null
) : IMutableObservable<T> {

    private val scope = CoroutineScope(dispatcher)
    private val mutex = Mutex()
    private val internalState = MutableStateFlow(initial)
    private val released = AtomicBoolean(false)

    private val numObserver = AtomicInteger(0)
    private val notified = AtomicInteger(0)
    private val suspendUntilValidChannels = LinkedList<Channel<T>>()
    private val suspendUntilValidMutex = Mutex()

    override var value: T
        get() = internalState.value
        set(value) {
            if (released.get()) return

            runBlocking {
                mutex.withLock {
                    suspendUntilValidMutex.withLock {
                        notified.set(0)
                    }
                    internalState.value = value
                }
            }
        }

    init {
        observer?.let {
            scope.launch {
                observe { observer(it) }
            }
        }
    }

    private fun buildOnChangedScope(unObserve: suspend () -> Unit = {}): MutableObservableChangedScope<T> =
        MutableObservableChangedScope(
            value,
            internalState.replayCache.firstOrNull() ?: value,
            internalState.replayCache,
            {
                internalState.resetReplayCache()
            },
            unObserve
        ) { value = it }

    override fun clearPreviousValues() = internalState.resetReplayCache()

    override suspend fun changeValue(block: suspend MutableObservableChangedScope<T>.(current: T) -> T) =
        mutex.withLock {
            internalState.value = buildOnChangedScope().block(value)
        }

    override suspend fun suspendUntil(timeout: ITimeSpan?, wanted: suspend (T) -> Boolean): T {
        val result = Channel<T>()
        mutex.withLock {
            if (wanted(value)) return value

            observe { value ->
                if (wanted(value)) {
                    result.send(value)
                    unObserve()
                }
            }
        }
        return timeout?.let { withTimeout(it.millis) { result.receive() } } ?: result.receive()
    }

    override suspend fun suspendUntilValid(timeout: ITimeSpan?): Boolean {
        val channel = Channel<T>()
        mutex.withLock {
            suspendUntilValidMutex.withLock {
                if (notified.get() == numObserver.get()) return false
                suspendUntilValidChannels += channel
            }
        }
        timeout?.millis?.let {
            try {
                withTimeout(it) { channel.receive() }
            } catch (te: TimeoutCancellationException) {
                return true
            }
        } ?: channel.receive()
        return false
    }

    private suspend fun notified() = suspendUntilValidMutex.withLock {
        if (notified.incrementAndGet() == numObserver.get()) {
            suspendUntilValidChannels.forEach { it.send(value) }
            suspendUntilValidChannels.clear()
        }
    }

    override suspend fun observe(
        changesOnly: Boolean,
        scope: CoroutineScope?,
        listener: MutableObserver<T, MutableObservableChangedScope<T>>
    ): suspend () -> Unit {
        numObserver.incrementAndGet()
        val unObserved = AtomicBoolean(false)
        lateinit var job: Job
        fun unObserve() {
            numObserver.decrementAndGet()
            unObserved.set(true)
            job.cancel()
        }
        job = this.scope.launch {
            internalState.collect { value ->
                if (!isActive || unObserved.get()) return@collect

                val previous = internalState.replayCache.getOrNull(1) ?: initial
                if (changesOnly && value != previous || !changesOnly)
                    buildOnChangedScope(::unObserve).listener(value)
                notified()
            }
        }
        return { unObserve() }
    }

    override fun toImmutableObservable(): IObservable<T> = Observable(this)

    override suspend fun release() {
        released.set(true)
        scope.cancel()
    }
}