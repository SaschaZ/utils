package dev.zieger.utils.coroutines.channel.parallel

import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.IPipeValue
import dev.zieger.utils.coroutines.channel.pipeline.IProcessingParams
import dev.zieger.utils.coroutines.channel.pipeline.ParallelProcessingType
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.sync.Mutex

interface IParallelProcessedValueMerger<out T : Any> {

    suspend fun IPipeValue<@UnsafeVariance T>.suspendUntilInPosition(send: suspend () -> Unit)
}

abstract class AbsParallelProcessedValueMerger<out T : Any>(protected open val params: IProcessingParams) :
    IParallelProcessedValueMerger<T> {

    private val mutex = Mutex()
    protected open var lastSend: IPipeValue<@UnsafeVariance T>? = null

    @Suppress("LeakingThis")
    protected open val valueSenderMap =
        SortedArrayList<ComparablePair<IPipeValue<@UnsafeVariance T>, suspend () -> Unit>>()

    protected open val IPipeValue<@UnsafeVariance T>.previousOutIdx
        get() = (if (parallelIdx == IPipeValue.NO_PARALLEL_EXECUTION
            || params.type == ParallelProcessingType.SAME
        ) outIdx - 1 else outIdx)// logV { "$this -> $it" }

    protected open val IPipeValue<@UnsafeVariance T>.previousParallelIdx
        get() = ((parallelIdx + params.numParallel - 1) % params.numParallel)// logV { "$this -> $it" }

    protected abstract val IPipeValue<@UnsafeVariance T>.isNext: Boolean

    private suspend fun processNextValidValues() = launchEx(mutex = mutex) {
        val pair = valueSenderMap.firstOrNull()
        if (pair?.first?.isNext == true) {
            val removed = valueSenderMap.removeFirstSync()
            lastSend = removed.first
            removed.second()
        }
    }.asUnit()

    override suspend fun IPipeValue<@UnsafeVariance T>.suspendUntilInPosition(send: suspend () -> Unit) {
//        Log.v("continuation $this")
        val continuation = Continuation()
        valueSenderMap.addSync(ComparablePair(this, suspend {
            send()
//            Log.v("send $this")
            continuation.trigger()
            processNextValidValues()
        }))
        processNextValidValues()
        continuation.suspendUntilTrigger()
    }
}

class ParallelProcessValueMerger<out T : Any>(params: IProcessingParams) : IParallelProcessedValueMerger<T>
by if (params.type == ParallelProcessingType.UNIQUE) ParallelUniqueProcessedValueMerger(params)
else ParallelSameProcessedValueMerger(params)