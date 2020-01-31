package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import de.gapps.utils.misc.to
import de.gapps.utils.time.duration.seconds
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import de.gapps.utils.log.Log

interface IPipelineObserver {

    var active: Boolean

    fun tick(element: IPipelineElement<*, *>, stage: PipelineElementStage, time: ITimeEx = TimeEx())
}

enum class PipelineElementStage {
    RECEIVE_INPUT,
    PROCESSING,
    SEND_OUTPUT,
    FINISHED_PROCESSING,
    FINISHED_CLOSING
}

open class PipelineObserver(private val scope: CoroutineScope = DefaultCoroutineScope()) : IPipelineObserver {

    private val tickChannel = Channel<Triple<IPipelineElement<*, *>, PipelineElementStage, ITimeEx>>()
    private val ticks = HashMap<IPipelineElement<*, *>, Pair<PipelineElementStage, ITimeEx>>()

    private var tickJob: Job? = null
    private var outputJob: Job? = null
    private val tickMutex = Mutex()

    override var active by OnChanged(false) {
        tickJob?.cancel()
        if (it) tickJob = scope.launchEx {
            for (tick in tickChannel) {
                tickMutex.withLock {
                    ticks[tick.first] = tick.second to tick.third
                }
                outputJob?.cancel()
                outputJob = scope.launchEx(delayed = 5.seconds) { printStates() }
            }
        }
    }

    private suspend fun printStates() = tickMutex.withLock {
        val sortedTicks = ArrayList(ticks.toList()).sortedBy { it.second.second }
        sortedTicks.forEach { tick ->
            Log.v("$tick")
        }
    }

    override fun tick(
        element: IPipelineElement<*, *>,
        stage: PipelineElementStage,
        time: ITimeEx
    ) = scope.launchEx { tickChannel.send(element to stage to time) }.asUnit()
}