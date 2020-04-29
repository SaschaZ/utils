@file:Suppress("ClassName", "unused")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.*
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import dev.zieger.utils.time.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

interface IProcessingWatchDog {

    val ticks: Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>
    fun tick(element: IProcessingUnit<*, *>, stage: ProcessingElementStage, time: ITimeEx = TimeEx())

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.numPipeElements: Int
        get() = size + entries.sumBy { (it.key as? IProcessingWatchDog)?.ticks?.numPipeElements ?: 0 }

    val IProcessingUnit<*, *>.numProcessed: Int
        get() = ticks[this]?.count { it.value == PROCESSING } ?: 0

    val IProcessingUnit<*, *>.numReceived: Int
        get() = ticks[this]?.count { it.value == RECEIVE_INPUT } ?: 0

    val IProcessingUnit<*, *>.numSend: Int
        get() = ticks[this]?.count { it.value == SEND_OUTPUT } ?: 0

    val IProcessingUnit<*, *>.hasFinished: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_PROCESSING } ?: false

    val IProcessingUnit<*, *>.hasClosed: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_CLOSING } ?: false

    val IProcessingUnit<*, *>.lastUpdateBefore: IDurationEx
        get() = TimeEx() - (ticks[this]?.maxBy { it.key }?.key ?: 0.toTime())

    val IProcessingUnit<*, *>.isSubPipeline: Boolean
        get() = this is IPipeline<*, *>

    val IProcessingUnit<*, *>.subWatchDog: IProcessingWatchDog?
        get() = this

    fun release()
}

sealed class ProcessingElementStage {
    object RECEIVE_INPUT : ProcessingElementStage()
    object PROCESSING : ProcessingElementStage()
    object SEND_OUTPUT : ProcessingElementStage()
    object FINISHED_PROCESSING : ProcessingElementStage()
    object FINISHED_CLOSING : ProcessingElementStage()
}

class ProcessingWatchDog private constructor(
    private val scope: CoroutineScope = DefaultCoroutineScope()
) : IProcessingWatchDog {

    companion object {
        private fun <V> AtomicReference<V>.updateAndGet2(updateFunction: (V) -> V): V {
            var prev: V?
            var next: V
            do {
                prev = get()
                next = updateFunction(prev)
            } while (!compareAndSet(prev, next))
            return next
        }

        private val watchDog = AtomicReference<IProcessingWatchDog?>(null)

        operator fun invoke(
            scope: CoroutineScope = DefaultCoroutineScope()
        ): IProcessingWatchDog = watchDog.updateAndGet2 { it ?: ProcessingWatchDog(scope) }!!
    }

    override val ticks = ConcurrentHashMap<IProcessingUnit<*, *>, ConcurrentHashMap<ITimeEx, ProcessingElementStage>>()

    private val tickMutex = Mutex()
    private val outputJob: Job

    init {
        outputJob = scope.launchEx(interval = 1.seconds, mutex = tickMutex) {
            printStates()
        }
    }

    private fun printStates() {
        println("#${hashCode()}-\n${ticks.entries.joinToString("\n") { (e, _) ->
            "\t${e.id}: ${e.numReceived}/${e.numProcessed}/${e.numSend}-${e.hasFinished}/${e.hasClosed}"
        }}")
    }

    override fun tick(
        element: IProcessingUnit<*, *>,
        stage: ProcessingElementStage,
        time: ITimeEx
    ) = scope.launchEx(mutex = tickMutex) {
        ticks.getOrPut(element) { ConcurrentHashMap() }[time] = stage
    }.asUnit()

    override fun release() = outputJob.cancel().asUnit()
}