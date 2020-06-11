package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.log.Log
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface IProcessingScope<out I : Any, out O : Any> : IConsumerScope<I>,
    IProducerScope<O>

@Suppress("LeakingThis")
open class ProcessingScope<out I : Any, out O : Any>(
    override val rawValue: IPipeValue<I>,
    producerScope: ProducerScope<O>
) : IProcessingScope<I, O>, IProducerScope<O> by producerScope {

    override val value: I = rawValue.value
    override val time: ITimeEx = rawValue.time
}

interface IProducerScope<out T : Any> : IParamsHolder {

    val inIdx: Int
    var outIdx: Int
    val parallelIdx: Int
    val parallelType: ParallelProcessingType
    val scope: CoroutineScopeEx

    suspend fun send(
        value: @UnsafeVariance T,
        time: ITimeEx = TimeEx(),
        outIdx: Int = this.outIdx++,
        parallelIdx: Int = IPipeValue.NO_PARALLEL_EXECUTION
    ) = send(
        PipeValue(
            value,
            time,
            inIdx,
            outIdx,
            parallelIdx
        )
    )

    suspend fun send(rawValue: IPipeValue<@UnsafeVariance T>): IPipeValue<@UnsafeVariance T>

    suspend fun close()
}

open class ProducerScope<out T : Any>(
    override val inIdx: Int,
    override var outIdx: Int,
    override val parallelIdx: Int = IPipeValue.NO_PARALLEL_EXECUTION,
    override val parallelType: ParallelProcessingType = ParallelProcessingType.NONE,
    override val params: IProcessingParams = ProcessingParams(),
    override val scope: CoroutineScopeEx,
    private val closer: suspend IProducerScope<T>.() -> Unit,
    private val sender: suspend (rawValue: IPipeValue<T>) -> Unit
) : IProducerScope<T> {

    constructor(
        parallelIdx: Int = IPipeValue.NO_PARALLEL_EXECUTION,
        parallelType: ParallelProcessingType = ParallelProcessingType.NONE,
        params: IProcessingParams = ProcessingParams(),
        scope: CoroutineScopeEx,
        closer: suspend IProducerScope<T>.() -> Unit,
        sender: suspend (rawValue: IPipeValue<T>) -> Unit
    ) : this(IPipeValue.NO_IDX, 0, parallelIdx, parallelType, params, scope, closer, sender)

    private val mutex: Mutex =
        Mutex()

    override suspend fun send(
        rawValue: IPipeValue<@UnsafeVariance T>
    ) = mutex.withLock { rawValue.withParallelIdx(parallelIdx, parallelType).apply { sender(this) } }

    override suspend fun close() {
        Log.v("producing finished")
        closer()
    }
}

interface IParamsHolder {
    val params: IProcessingParams
}

interface IConsumerScope<out T : Any> : IParamsHolder {

    val rawValue: IPipeValue<T>
    val value: T
    val inIdx: Int
    val outIdx: Int
    val time: ITimeEx
    val scope: CoroutineScopeEx
}

@Suppress("LeakingThis")
open class ConsumerScope<out T : Any>(
    override val rawValue: IPipeValue<T>,
    override val params: IProcessingParams,
    override val scope: CoroutineScopeEx
) : IConsumerScope<T> {

    override val value: T = rawValue.value
    override val inIdx: Int = rawValue.inIdx
    override val outIdx: Int = rawValue.outIdx
    override val time: ITimeEx = rawValue.time
}