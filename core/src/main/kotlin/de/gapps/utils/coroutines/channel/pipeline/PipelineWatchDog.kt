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

interface IPipelineWatchDog {

    var active: Boolean
    val ticks: Map<IPipelineElement<*, *>, Map<ITimeEx, PipelineElementStage>>
    fun tick(element: IPipelineElement<*, *>, stage: PipelineElementStage, time: ITimeEx = TimeEx())

    val Map<IPipelineElement<*, *>, Map<ITimeEx, PipelineElementStage>>.numPipeElements: Int
        get() = size + entries.sumBy { (it.key as? IPipelineWatchDog)?.ticks?.numPipeElements ?: 0 }

    val IPipelineElement<*, *>.numProcessed: Int
        get() = ticks[this]?.count { it.value == PROCESSING } ?: 0

    val IPipelineElement<*, *>.numReceived: Int
        get() = ticks[this]?.count { it.value == RECEIVE_INPUT } ?: 0

    val IPipelineElement<*, *>.numSend: Int
        get() = ticks[this]?.count { it.value == SEND_OUTPUT } ?: 0

    val IPipelineElement<*, *>.hasFinished: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_PROCESSING } ?: false

    val IPipelineElement<*, *>.hasClosed: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_CLOSING } ?: false

    val IPipelineElement<*, *>.lastUpdateBefore: IDurationEx
        get() = TimeEx() - (ticks[this]?.maxBy { it.key }?.key ?: 0.toTime())

    val IPipelineElement<*, *>.isSubPipeline: Boolean
        get() = this is IPipeline<*, *>

    val IPipelineElement<*, *>.subWatchDog: IPipelineWatchDog?
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

    private val tickChannel = Channel<Triple<IPipelineElement<*, *>, PipelineElementStage, ITimeEx>>()
    override val ticks = HashMap<IPipelineElement<*, *>, HashMap<ITimeEx, PipelineElementStage>>()

    private var tickJob: Job? = null
    private var outputJob: Job? = null
    private val tickMutex = Mutex()

    override var active by OnChanged(active) {
        tickJob?.cancel()
        if (it) tickJob = scope.launchEx {
            for ((element, stage, time) in tickChannel) {
                tickMutex.withLock {
                    ticks.getOrPut(element) { HashMap() }.put(time, stage)
                }
            }
        }
    }

    private var updateJob: Job? = null
    private suspend fun printStates(ongoing: Boolean = true): Unit = tickMutex.withLock {
        printStatesInternal()

        if (ongoing)
            updateJob = launchEx(delayed = 1.seconds) { printStates() }
    }

    private fun printStatesInternal() {
        println(ticks.entries.joinToString("\n") { (e, _) ->
            "${e.id}: ${e.numReceived}/${e.numProcessed}/${e.numSend}-${e.hasFinished}/${e.hasClosed}"
        })
    }

    override fun tick(
        element: IPipelineElement<*, *>,
        stage: PipelineElementStage,
        time: ITimeEx
    ) = scope.launchEx { tickChannel.send(element to stage to time) }.asUnit()
}