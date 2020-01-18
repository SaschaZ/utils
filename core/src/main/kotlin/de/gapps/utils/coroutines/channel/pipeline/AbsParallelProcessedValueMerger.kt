package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.Continuation
import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.misc.ifN
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap


interface IParallelProcessedValueMerger<out T : Any> {

    suspend fun IPipeValue<@UnsafeVariance T>.suspendUntilInPosition(send: suspend () -> Unit)
}

abstract class AbsParallelProcessedValueMerger<out T : Any>(protected open val params: IProcessingParams) :
    IParallelProcessedValueMerger<T> {

    private val mutex = Mutex()
    protected open var lastSend: IPipeValue<@UnsafeVariance T>? = null

    @Suppress("LeakingThis")
    protected open val valueSenderMap =
        ConcurrentHashMap<IPipeValue<@UnsafeVariance T>, suspend () -> Unit>(params.numParallel)
    protected open val valueSenderList
        get() = valueSenderMap.toList()
    protected open val valueList
        get() = valueSenderMap.keys
    protected open val senderList
        get() = valueSenderMap.values

    protected open val IPipeValue<@UnsafeVariance T>.previousOutIdx
        get() = (if (parallelIdx == IPipeValue.NO_PARALLEL_EXECUTION
            || params.type == ParallelProcessingTypes.SAME
        ) outIdx - 1 else outIdx) logV { "previousOutIdx=$it" }
    protected open val IPipeValue<@UnsafeVariance T>.previousParallelIdx
        get() = ((parallelIdx + params.numParallel - 1) % params.numParallel) logV { "previousParallelIdx=$it" }

    protected abstract val IPipeValue<@UnsafeVariance T>.canSend: Boolean

    protected open suspend fun IPipeValue<@UnsafeVariance T>.nextWaitingValue(): IPipeValue<T>? =
        mutex.withLock { valueList.find { it.canSend } } ifN {
            Log.w("no next waiting value found ($this)")
            null
        }

    protected open suspend fun IPipeValue<@UnsafeVariance T>.suspendUntilSend(send: suspend () -> Unit) {
        Log.v("before suspending $this")
        val continuation = Continuation()
        valueSenderMap[this] = suspend { Log.v("continuation"); continuation.trigger() }
        continuation.suspendUntilTrigger()

        Log.v("after suspend and before lock")
        mutex.withLock {
            valueSenderMap.remove(this)
            lastSend = this logV { "send $it" }
        }
        send()
    }

    protected open suspend fun IPipeValue<@UnsafeVariance T>.sendValidWaits(send: suspend () -> Unit): Boolean {
        return mutex.withLock {
            if (canSend) {
                Log.v("send $this")
                send()
                true
            } else false
        }
    }

    override suspend fun IPipeValue<@UnsafeVariance T>.suspendUntilInPosition(send: suspend () -> Unit) {
        if (sendValidWaits { send() })
            nextWaitingValue()?.run { sendValidWaits { valueSenderMap[this]?.invoke() } }
        else suspendUntilSend(send)
    }
}

class ParallelProcessValueMerger<out T : Any>(params: IProcessingParams) : IParallelProcessedValueMerger<T>
by if (params.type == ParallelProcessingTypes.UNIQUE) ParallelUniqueProcessedValueMerger(params)
else ParallelSameProcessedValueMerger(params)