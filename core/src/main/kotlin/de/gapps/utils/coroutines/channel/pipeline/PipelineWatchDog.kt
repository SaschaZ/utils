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

    fun tick(element: IPipelineElement<*, *>, stage: PipelineElementStage, time: ITimeEx = TimeEx())
}

sealed class PipelineElementStage {
    object RECEIVE_INPUT : PipelineElementStage()
    object PROCESSING : PipelineElementStage()
    object SEND_OUTPUT : PipelineElementStage()
    object FINISHED_PROCESSING : PipelineElementStage()
    object FINISHED_CLOSING : PipelineElementStage()
}

open class PipelineWatchDog(private val scope: CoroutineScope = DefaultCoroutineScope()) : IPipelineWatchDog {

    private val tickChannel = Channel<Triple<IPipelineElement<*, *>, PipelineElementStage, ITimeEx>>()
    private val ticks = HashMap<IPipelineElement<*, *>, HashMap<ITimeEx, PipelineElementStage>>()

    private var tickJob: Job? = null
    private var outputJob: Job? = null
    private val tickMutex = Mutex()

    override var active by OnChanged(false) {
        tickJob?.cancel()
        if (it) tickJob = scope.launchEx {
            for ((element, stage, time) in tickChannel) {
                tickMutex.withLock {
                    ticks.getOrPut(element) { HashMap() }.put(time, stage)
                }
            }
        }
    }

    private val IPipelineElement<*, *>.numProcessed: Int
        get() = ticks[this]?.count { it.value == PROCESSING } ?: 0

    private val IPipelineElement<*, *>.numReceived: Int
        get() = ticks[this]?.count { it.value == RECEIVE_INPUT } ?: 0

    private val IPipelineElement<*, *>.numSend: Int
        get() = ticks[this]?.count { it.value == SEND_OUTPUT } ?: 0

    private val IPipelineElement<*, *>.hasFinished: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_PROCESSING } ?: false

    private val IPipelineElement<*, *>.hasClosed: Boolean
        get() = ticks[this]?.any { it.value == FINISHED_CLOSING } ?: false

    private val IPipelineElement<*, *>.lastUpdateBefore: IDurationEx
        get() = TimeEx() - (ticks[this]?.maxBy { it.key }?.key ?: 0.toTime())

    private var updateJob: Job? = null
    private suspend fun printStates(ongoing: Boolean = true): Unit = tickMutex.withLock {


        if (ongoing)
            updateJob = launchEx(delayed = 1.seconds) { printStates() }
    }

    override fun tick(
        element: IPipelineElement<*, *>,
        stage: PipelineElementStage,
        time: ITimeEx
    ) = scope.launchEx { tickChannel.send(element to stage to time) }.asUnit()
}