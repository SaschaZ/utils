@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.observables

import dev.zieger.utils.time.ITimeSpan
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface IMutableObservableBase<O, T, S : IMutableObservableChangedScope<T>, IT : IObservableBase<O, T>> :
    IObservableBase<O, T>, ReadOnlyProperty<O, T> {

    override val value: T

    val primaryScope: CoroutineScope
    val secondaryScope: CoroutineScope

    override fun getValue(thisRef: O, property: KProperty<*>): T {
        owner = thisRef
        return value
    }

    suspend fun changeValue(block: suspend S.(current: T) -> T): T

    suspend fun setValue(value: T) = changeValue { value }
    fun offerValue(value: T) = primaryScope.launch { setValue(value) }

    suspend fun observe(
        changesOnly: Boolean = true,
        notifyForInitial: Boolean = false,
        scope: CoroutineScope? = null,
        context: CoroutineContext? = null,
        listener: MutableObserver<T, S>
    ): suspend () -> Unit

    suspend fun veto(block: suspend S.(current: T) -> Boolean): suspend () -> Unit

    suspend fun map(block: suspend S.(current: T) -> T): suspend () -> Unit

    fun toImmutableObservable(): IT
}

typealias IMutableObservable<T> = IMutableObservableBase<Any?, T, IMutableObservableChangedScope<T>, IObservable<T>>
typealias IMutableOwnedObservable<O, T> = IMutableObservableBase<O, T, IMutableOwnedObservableChangedScope<O, T>, IOwnedObservable<O, T>>

open class MutableObservable<T>(
    initial: T,
    changesOnly: Boolean = true,
    notifyForInitial: Boolean = false,
    primaryContext: CoroutineContext = ObservableDispatcherHolder.primaryContext,
    secondaryContext: CoroutineContext = ObservableDispatcherHolder.secondaryContext,
    veto: (suspend IMutableObservableChangedScope<T>.(current: T) -> Boolean)? = null,
    map: (suspend IMutableObservableChangedScope<T>.(current: T) -> T)? = null,
    initialObserver: MutableObserver<T, IMutableObservableChangedScope<T>>? = null
) : IMutableObservable<T>,
    MutableObservableBase<Any?, T, IMutableObservableChangedScope<T>, IObservable<T>>(
        initial,
        changesOnly,
        notifyForInitial,
        primaryContext,
        secondaryContext,
        veto,
        map,
        initialObserver
    ) {

    override var owner: Any? = null

    override fun buildOnChangedScope(unObserve: suspend () -> Unit): IMutableObservableChangedScope<T> =
        MutableObservableChangedScope(
            internalValue,
            previousValues.lastOrNull(),
            previousValues,
            { clearPreviousValues() },
            unObserve,
            { value -> secondaryScope.launch { changeValue { value } } })

    override fun toImmutableObservable(): IObservable<T> = Observable(this)
}

class MutableOwnedObservable<O, T>(
    initial: T,
    changesOnly: Boolean = true,
    notifyForInitial: Boolean = false,
    primaryContext: CoroutineContext = ObservableDispatcherHolder.primaryContext,
    secondaryContext: CoroutineContext = ObservableDispatcherHolder.secondaryContext,
    veto: (suspend IMutableOwnedObservableChangedScope<O, T>.(current: T) -> Boolean)? = null,
    map: (suspend IMutableOwnedObservableChangedScope<O, T>.(current: T) -> T)? = null,
    initialObserver: MutableObserver<T, IMutableOwnedObservableChangedScope<O, T>>? = null
) : IMutableOwnedObservable<O, T>,
    MutableObservableBase<O, T, IMutableOwnedObservableChangedScope<O, T>, IOwnedObservable<O, T>>(
        initial,
        changesOnly,
        notifyForInitial,
        primaryContext,
        secondaryContext,
        veto,
        map,
        initialObserver
    ) {

    override var owner: O? = null

    override fun buildOnChangedScope(unObserve: suspend () -> Unit): IMutableOwnedObservableChangedScope<O, T> =
        MutableOwnedObservableChangedScope(
            owner,
            internalValue,
            previousValues.last(),
            previousValues,
            { clearPreviousValues() },
            unObserve,
            { value -> secondaryScope.launch { changeValue { value } } })

    override fun toImmutableObservable(): IOwnedObservable<O, T> = OwnedObservable(this)
}

abstract class MutableObservableBase<O, T, S : IMutableObservableChangedScope<T>, IT : IObservableBase<O, T>>(
    initial: T,
    changesOnly: Boolean = true,
    notifyForInitial: Boolean = false,
    primaryContext: CoroutineContext = ObservableDispatcherHolder.primaryContext,
    secondaryContext: CoroutineContext = ObservableDispatcherHolder.secondaryContext,
    veto: (suspend S.(current: T) -> Boolean)? = null,
    map: (suspend S.(current: T) -> T)? = null,
    initialObserver: MutableObserver<T, S>? = null
) : IMutableObservableBase<O, T, S, IT> {

    private val valueMutex = Mutex()

    protected var internalValue: T = initial

    override val value: T
        get() = internalValue
    private var channelValue: T = initial

    internal var previousValues = LinkedList<T>()

    private val observerMutex = Mutex()
    private val observer = LinkedList<MutableObserver<T, S>>()

    private val vetoMutex = Mutex()
    private var veto: (suspend S.(current: T) -> Boolean)? = null

    private val mapMutex = Mutex()
    private var map: (suspend S.(current: T) -> T)? = null

    private val valueWaiterMutex = Mutex()
    private val valueWaiter = HashMap<suspend (T) -> Boolean, suspend (T) -> Boolean>()

    override val primaryScope = CoroutineScope(primaryContext)
    override val secondaryScope = CoroutineScope(secondaryContext)

    init {
        runBlocking {
            initialObserver?.also {
                observe(
                    changesOnly = changesOnly,
                    notifyForInitial = notifyForInitial,
                    listener = it
                )
            }
            veto?.let { this@MutableObservableBase.veto(it) }
            map?.let { this@MutableObservableBase.map(it) }
        }
    }

    private suspend fun onPropertyChanged() {
        observerMutex.withLock {
            observer.forEach {
                buildOnChangedScope { primaryScope.launch { observerMutex.withLock { observer.remove(it) } } }.run {
                    it(current)
                }
            }
        }
        valueWaiterMutex.withLock {
            channelValue = value
            valueWaiter.filter { (key, value) ->
                buildOnChangedScope { primaryScope.launch { valueWaiterMutex.withLock { valueWaiter.remove(key) } } }.run {
                    key(current) && value(current)
                }
            }.onEach { (key, _) -> valueWaiter.remove(key) }
        }
    }

    protected abstract fun buildOnChangedScope(unObserve: suspend () -> Unit = {}): S

    override suspend fun changeValue(block: suspend S.(current: T) -> T): T {
        valueMutex.withLock {
            val mappedValue = mapMutex.withLock {
                map?.run {
                    buildOnChangedScope { mapMutex.withLock { map = null } }.run { invoke(this, value) }
                }
            }

            val isVeto = vetoMutex.withLock {
                veto?.run {
                    buildOnChangedScope { vetoMutex.withLock { veto = null } }.run {
                        invoke(this, mappedValue ?: value)
                    }
                }
            } ?: false

            if (!isVeto) {
                previousValues += value
                internalValue = buildOnChangedScope().block(value)
            }

            if (!isVeto)
                onPropertyChanged()
        }

        return internalValue
    }

    override suspend fun suspendUntil(timeout: ITimeSpan?, wanted: suspend (T) -> Boolean): T? {
        val result = Channel<T>(Channel.UNLIMITED)
        val (channelSet, checkedValue) = valueMutex.withLock {
            valueWaiterMutex.withLock {
                if (!wanted(value)) {
                    valueWaiter[wanted] = {
                        result.send(it)
                        true
                    }
                    true to value
                } else false to value
            }
        }

        return if (channelSet) {
            timeout?.millis?.let {
                try {
                    withTimeout(it) { result.receive() }
                } catch (te: TimeoutCancellationException) {
                    return null
                }
            } ?: result.receive()
        } else checkedValue
    }

    override suspend fun observe(
        changesOnly: Boolean,
        notifyForInitial: Boolean,
        scope: CoroutineScope?,
        context: CoroutineContext?,
        listener: MutableObserver<T, S>
    ): suspend () -> Unit {
        val l: MutableObserver<T, S> = {
            if (previous != current || !changesOnly)
                scope?.let { it.launch { listener(current) } }
                    ?: context?.let { withContext(it) { listener(current) } }
                    ?: listener(current)
        }
        if (notifyForInitial)
            valueMutex.withLock { buildOnChangedScope { observerMutex.withLock { observer.remove(l) } } }
                .run { l.invoke(this, current) }
        observerMutex.withLock { observer += l }
        return { observerMutex.withLock { observer.remove(l) } }
    }

    override suspend fun veto(block: suspend S.(current: T) -> Boolean): suspend () -> Unit {
        vetoMutex.withLock {
            veto?.also { throw IllegalStateException("veto already set") }
            veto = block
        }
        return { vetoMutex.withLock { veto = null } }
    }

    override suspend fun map(block: suspend S.(current: T) -> T): suspend () -> Unit {
        mapMutex.withLock {
            map?.also { throw IllegalStateException("map already set") }
            map = block
        }
        return { mapMutex.withLock { map = null } }
    }

    override fun clearPreviousValues() {
        previousValues = LinkedList()
    }

    override suspend fun release() {
        primaryScope.cancel()
        secondaryScope.cancel()
    }
}

//open class MutableObservable<T>(
//    private val initial: T,
//    dispatcher: CoroutineContext = ObservableDispatcherHolder.context,
//    observer: MutableObserver<T, ObservableChangedScope<T>>? = null
//) : IMutableObservable<T> {
//
//    private val scope = CoroutineScope(dispatcher)
//    private val mutex = Mutex()
//    private val internalState = MutableStateFlow(initial)
//    private val released = AtomicBoolean(false)
//
//    private val numObserver = AtomicInteger(0)
//    private val notified = AtomicInteger(0)
//    private val suspendUntilValidChannels = LinkedList<Channel<T>>()
//    private val suspendUntilValidMutex = Mutex()
//
//    override var value: T
//        get() = internalState.value
//        set(value) {
//            if (released.get()) return
//
//            runBlocking {
//                mutex.withLock {
//                    suspendUntilValidMutex.withLock {
//                        notified.set(0)
//                    }
//                    internalState.value = value
//                }
//            }
//        }
//
//    init {
//        observer?.let {
//            scope.launch {
//                observe { observer(it) }
//            }
//        }
//    }
//
//    private fun buildOnChangedScope(unObserve: suspend () -> Unit = {}): MutableObservableChangedScope<T> =
//        MutableObservableChangedScope(
//            value,
//            internalState.replayCache.firstOrNull() ?: value,
//            internalState.replayCache,
//            {
//                internalState.resetReplayCache()
//            },
//            unObserve
//        ) { value = it }
//
//    override fun clearPreviousValues() = internalState.resetReplayCache()
//
//    override suspend fun changeValue(block: suspend MutableObservableChangedScope<T>.(current: T) -> T) =
//        mutex.withLock {
//            internalState.value = buildOnChangedScope().block(value)
//        }
//
//    override suspend fun suspendUntil(timeout: ITimeSpan?, wanted: suspend (T) -> Boolean): T {
//        val result = Channel<T>()
//        mutex.withLock {
//            if (wanted(value)) return value
//
//            observe { value ->
//                if (wanted(value)) {
//                    result.send(value)
//                    unObserve()
//                }
//            }
//        }
//        return timeout?.let { withTimeout(it.millis) { result.receive() } } ?: result.receive()
//    }
//
//    override suspend fun suspendUntilValid(timeout: ITimeSpan?): Boolean {
//        val channel = Channel<T>()
//        mutex.withLock {
//            suspendUntilValidMutex.withLock {
//                if (notified.get() == numObserver.get()) return false
//                suspendUntilValidChannels += channel
//            }
//        }
//        timeout?.millis?.let {
//            try {
//                withTimeout(it) { channel.receive() }
//            } catch (te: TimeoutCancellationException) {
//                return true
//            }
//        } ?: channel.receive()
//        return false
//    }
//
//    private suspend fun notified() = suspendUntilValidMutex.withLock {
//        if (notified.incrementAndGet() == numObserver.get()) {
//            suspendUntilValidChannels.forEach { it.send(value) }
//            suspendUntilValidChannels.clear()
//        }
//    }
//
//    override suspend fun observe(
//        changesOnly: Boolean,
//        scope: CoroutineScope?,
//        listener: MutableObserver<T, MutableObservableChangedScope<T>>
//    ): suspend () -> Unit {
//        numObserver.incrementAndGet()
//        val unObserved = AtomicBoolean(false)
//        lateinit var job: Job
//        fun unObserve() {
//            numObserver.decrementAndGet()
//            unObserved.set(true)
//            job.cancel()
//        }
//        job = this.scope.launch {
//            internalState.collect { value ->
//                if (!isActive || unObserved.get()) return@collect
//
//                val previous = internalState.replayCache.getOrNull(1) ?: initial
//                if (changesOnly && value != previous || !changesOnly)
//                    buildOnChangedScope(::unObserve).listener(value)
//                notified()
//            }
//        }
//        return { unObserve() }
//    }
//
//    override suspend fun veto(block: suspend (T) -> Boolean) {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun map(block: suspend (T) -> T) {
//        TODO("Not yet implemented")
//    }
//
//    override fun toImmutableObservable(): IObservable<T> = Observable(this)
//
//    override suspend fun release() {
//        released.set(true)
//        scope.cancel()
//    }
//}