package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

open class SimpleObservable<T>(
    initial: T,
    private val context: CoroutineContext = ObservableDispatcherHolder.context
) : IMutableObservable<T> {

    private val valueMutex = Mutex()

    private var internalValue: T = initial

    override var value: T
        get() = internalValue
        set(value) = runBlocking(context) {
            valueMutex.withLock {
                previousValues += internalValue
                internalValue = value
            }
            onPropertyChanged()
        }
    private var channelValue: T = initial

    private var previousValues = LinkedList<T>()

    private val observerMutex = Mutex()
    private val observer = LinkedList<MutableObserver<T, MutableObservableChangedScope<T>>>()

    private val valueWaiterMutex = Mutex()
    private val valueWaiter = HashMap<suspend (T) -> Boolean, suspend (T) -> Boolean>()

    private val scope = CoroutineScope(context)
    private val emptyScope = CoroutineScope(EmptyCoroutineContext)

    private val onChangedChannel =
        Channel<Pair<suspend () -> Unit, MutableObservableChangedScope<T>>>(Channel.RENDEZVOUS)

    init {
        scope.launch {
            for ((callback, scope) in onChangedChannel) {
                observerMutex.withLock {
                    observer.forEach {
                        scope.run {
                            unObserve = { observerMutex.withLock { observer.remove(it) } }
                            it(scope.current)
                        }
                    }
                }
                valueWaiterMutex.withLock {
                    channelValue = scope.current
                    valueWaiter.filter { (key, value) -> key(scope.current) && value(scope.current) }
                        .onEach { (key, _) -> valueWaiter.remove(key) }
                }
                callback()
            }
        }
    }

    private suspend fun onPropertyChanged() {
        val channel = Channel<Boolean>()
        onChangedChannel.send(suspend { channel.send(true) } to buildOnChangedScope())
        channel.receive()
    }

    private fun buildOnChangedScope(unObserve: suspend () -> Unit = {}): MutableObservableChangedScope<T> =
        MutableObservableChangedScope(
            internalValue,
            previousValues.last(),
            previousValues,
            { clearPreviousValues() },
            unObserve,
            { emptyScope.launch { value = it } })

    override suspend fun changeValue(block: suspend MutableObservableChangedScope<T>.(current: T) -> T) {
        valueMutex.withLock {
            previousValues += value
            internalValue = buildOnChangedScope().block(value)
        }
        onPropertyChanged()
    }

    override suspend fun suspendUntil(timeout: ITimeSpan?, wanted: suspend (T) -> Boolean): T {
        val result = Channel<T>(Channel.UNLIMITED)
        val channelSet = valueWaiterMutex.withLock {
            (channelValue != value || !wanted(value)).also {
                valueWaiter[wanted] = {
                    result.send(it)
                    true
                }
            }
        }

        return if (channelSet) {
            timeout?.millis?.let {
                try {
                    withTimeout(it) { result.receive() }
                } catch (te: TimeoutCancellationException) {
                    return value
                }
            } ?: result.receive()
        } else value
    }

    override suspend fun observe(
        changesOnly: Boolean,
        scope: CoroutineScope?,
        listener: MutableObserver<T, MutableObservableChangedScope<T>>
    ): suspend () -> Unit {
        val l: MutableObserver<T, MutableObservableChangedScope<T>> = {
            if (previous != current || !changesOnly)
                scope?.let { it.launch { listener(current) } } ?: listener(current)
        }
        observerMutex.withLock { observer += l }
        return { observerMutex.withLock { observer.remove(l) } }
    }

    override fun clearPreviousValues() {
        previousValues = LinkedList()
    }

    override fun toImmutableObservable(): IObservable<T> = Observable(this)

    override suspend fun release() {
        scope.cancel()
        emptyScope.cancel()
    }
}