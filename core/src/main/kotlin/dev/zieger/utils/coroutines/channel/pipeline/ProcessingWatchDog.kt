@file:Suppress("ClassName", "unused")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.parallel.ParallelProcessor
import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.*
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.*
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

    val ticks: MutableMap<IProcessingUnit<*, *>, MutableMap<ITimeEx, ProcessingElementStage>>
    fun IProcessingUnit<*, *>.tick(stage: ProcessingElementStage, time: ITimeEx = TimeEx())

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.producer: Map<IProducer<*>, Map<ITimeEx, ProcessingElementStage>>?
        get() = filter { it.key.isProducer }

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.processor: Map<IProcessor<*, *>, Map<ITimeEx, ProcessingElementStage>>?
        get() = filter { it.key.isProcessor }

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.parallelProcessor: Map<ParallelProcessor<*, *>, Map<ITimeEx, ProcessingElementStage>>?
        get() = filter { it.key.isParallelProcessor }

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.pipeline: Map<IPipeline<*, *>, Map<ITimeEx, ProcessingElementStage>>?
        get() = filter { it.key.isPipeline }

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.consumer: Map<IConsumer<*>, Map<ITimeEx, ProcessingElementStage>>?
        get() = filter { it.key.isConsumer }

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.numPipeElements: Int
        get() = size + entries.sumBy { (it.key as? IProcessingWatchDog)?.ticks?.numPipeElements ?: 0 }

    val IProcessingUnit<*, *>.numProcessed: Int
        get() = ticks[this]?.count { it.value == PROCESSING } ?: 0

    val IProcessingUnit<*, *>.numReceived: Int
        get() = ticks[this]?.count { it.value == RECEIVE_INPUT } ?: 0

    val IProcessingUnit<*, *>.numSend: Int
        get() = ticks[this]?.count { it.value == SEND_OUTPUT } ?: 0

    val IProcessingUnit<*, *>.isWaitingSince: IDurationEx?
        get() = ticks[this]?.entries?.maxBy { it.key }?.run {
            if (value == RECEIVE_INPUT)
                TimeEx() - key
            else null
        }

    val IProcessingUnit<*, *>.hasFinished: Boolean
        get() = ticks[this]?.any { it.value.anyOf(FINISHED_PROCESSING, FINISHED_CONSUMING, FINISHED_PRODUCING) }
            ?: false

    val IProcessingUnit<*, *>.hasClosed: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_CLOSING } ?: false

    val IProcessingUnit<*, *>.lastUpdateBefore: IDurationEx
        get() = TimeEx() - (ticks[this]?.maxBy { it.key }?.key ?: 0.toTime())

    fun release()
}

sealed class ProcessingElementStage {
    object PRODUCING : ProcessingElementStage()
    object FINISHED_PRODUCING : ProcessingElementStage()

    object RECEIVE_INPUT : ProcessingElementStage()
    object PROCESSING : ProcessingElementStage()
    object SEND_OUTPUT : ProcessingElementStage()
    object FINISHED_PROCESSING : ProcessingElementStage()
    object FINISHED_CLOSING : ProcessingElementStage()

    object CONSUMING : ProcessingElementStage()
    object FINISHED_CONSUMING : ProcessingElementStage()
}

open class ProcessingWatchDog protected constructor(
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    printInterval: IDurationEx = 1.seconds
) : IProcessingWatchDog {

    companion object {
        private val watchDog = AtomicReference<IProcessingWatchDog?>(null)

        @Suppress("MemberVisibilityCanBePrivate")
        var replacement: IProcessingWatchDog? = null

        operator fun invoke(
            scope: CoroutineScope = DefaultCoroutineScope(),
            printInterval: IDurationEx = 1.seconds
        ): IProcessingWatchDog = replacement
            ?: watchDog.updateAndGetLegacy { it ?: ProcessingWatchDog(scope, printInterval) }!!
    }

    override val ticks = ConcurrentHashMap<IProcessingUnit<*, *>, MutableMap<ITimeEx, ProcessingElementStage>>()

    private val tickMutex = Mutex()
    private var outputJob: Job? = null

    var watchDogActive by OnChanged(true, notifyForExisting = true) {
        outputJob?.cancel()
        if (it) {
            outputJob = scope.launchEx(interval = printInterval, mutex = tickMutex) {
                printStates(ticks)
            }
        }
    }

    private fun Map<out IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.printStats(numTabs: Int = 1) {
        println(entries.joinToString("\n") { (e, _) ->
            "${(0 until numTabs).joinToString("") { "\t" }}${e.id}: " +
                    "${e.numReceived}/${e.numProcessed}/${e.numSend}-" +
                    when {
                        e.hasClosed -> "closed"
                        e.hasFinished -> "finished"
                        e.isWaitingSince != null -> "waiting(${e.isWaitingSince})"
                        else -> "active(${e.lastUpdateBefore.formatDuration()})"
                    }
        })
    }

    protected open fun printStates(ticks: Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>) {
        ticks.pipeline?.run {
            println("\tPipeline")
            printStats(1)
        }
        ticks.producer?.run {
            println("\tProducer:")
            printStats(2)
        }
        ticks.parallelProcessor?.run {
            println("\tParallelProcessor")
            printStats(2)
        }
        ticks.processor?.run {
            println("\tProcessor:")
            printStats(2)
        }
        ticks.consumer?.run {
            println("\tConsumer:")
            printStats(2)
        }
        println("\n")
    }

    override fun IProcessingUnit<*, *>.tick(
        stage: ProcessingElementStage,
        time: ITimeEx
    ) = scope.launchEx(mutex = tickMutex) {
        val element: IProcessingUnit<*, *> = this@tick
        ticks.getOrPut(element) { ConcurrentHashMap() }[time] = stage
    }.asUnit()

    override fun release() = outputJob?.cancel().asUnit()
}

private inline fun <reified T : IProcessingUnit<*, *>> Map<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>.filter(
    filter: (Map.Entry<IProcessingUnit<*, *>, Map<ITimeEx, ProcessingElementStage>>) -> Boolean
): Map<T, Map<ITimeEx, ProcessingElementStage>>? =
    entries.filter(filter).mapNotNull { it.key.castSafe<T>()?.let { s -> s to it.value } }
        .nullWhen { it.isEmpty() }?.toMap()