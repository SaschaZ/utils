@file:Suppress("ClassName")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.PipelineElementStage.*
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.to
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.seconds
import de.gapps.utils.time.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

interface IPipelineWatchDog {

    var watchDogActive: Boolean
    val ticks: Map<IProcessingUnit<*, *>, Map<ITimeEx, PipelineElementStage>>
    fun tick(element: IProcessingUnit<*, *>, stage: PipelineElementStage, time: ITimeEx = TimeEx())

    val Map<IProcessingUnit<*, *>, Map<ITimeEx, PipelineElementStage>>.numPipeElements: Int
        get() = size + entries.sumBy { (it.key as? IPipelineWatchDog)?.ticks?.numPipeElements ?: 0 }

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

    val IProcessingUnit<*, *>.subWatchDog: IPipelineWatchDog?
        get() = this as? IPipelineWatchDog
}

sealed class PipelineElementStage {
    object RECEIVE_INPUT : PipelineElementStage()
    object PROCESSING : PipelineElementStage()
    object SEND_OUTPUT : PipelineElementStage()
    object FINISHED_PROCESSING : PipelineElementStage()
    object FINISHED_CLOSING : PipelineElementStage()
}

open class PipelineWatchDog(
    private val scope: CoroutineScope = DefaultCoroutineScope(),
    active: Boolean = false
) : IPipelineWatchDog {

    private val tickChannel = Channel<Triple<IProcessingUnit<*, *>, PipelineElementStage, ITimeEx>>()
    override val ticks = ConcurrentHashMap<IProcessingUnit<*, *>, ConcurrentHashMap<ITimeEx, PipelineElementStage>>()

    private var tickJob: Job? = null
    private var outputJob: Job? = null
    private val tickMutex = Mutex()

    override var watchDogActive by OnChanged(active, notifyForExisting = true) {
        outputJob?.cancel()
        if (it) outputJob = scope.launchEx(interval = 1.seconds, mutex = tickMutex) {
            printStates()
        }
    }

    init {
        tickJob = scope.launchEx {
            for ((element, stage, time) in tickChannel) {
                tickMutex.withLock {
                    ticks.getOrPut(element) { ConcurrentHashMap() }.put(time, stage)
                }
            }
        }
    }

    private fun printStates() {
        println("#${ticks.entries.joinToString("\n") { (e, _) ->
            "${e.id}: ${e.numReceived}/${e.numProcessed}/${e.numSend}-${e.hasFinished}/${e.hasClosed}"
        }}")
    }

    override fun tick(
        element: IProcessingUnit<*, *>,
        stage: PipelineElementStage,
        time: ITimeEx
    ) = scope.launchEx { tickChannel.send(element to stage to time) }.asUnit()
}