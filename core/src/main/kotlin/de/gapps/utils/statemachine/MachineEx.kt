@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base interface for an event of [MachineEx]
 */
interface IEvent

/**
 * Base interface for any event data of [MachineEx]
 */
interface IData

/**
 * Base interface for a state of [MachineEx]
 */
interface IState


/**
 * TODO
 */
open class MachineEx<out D : IData, out E : IEvent, out S : IState>(
    private val initialState: S,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    builder: IMachineEx<D, E, S>.() -> Unit
) : IMachineEx<D, E, S> {

    override val mapper: IMachineExMapper<D, E, S> = MachineExMapper()

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventMutex = Mutex()
    private val eventChannel = Channel<E>(Channel.BUFFERED)

    override var event: @UnsafeVariance E?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()

    override val previousEvents: MutableList<@UnsafeVariance E>  = ArrayList()

    override val state: S
        get() = previousStates.lastOrNull() ?: initialState
    override val previousStates: MutableList<@UnsafeVariance S> = ArrayList()

    private val previousChanges: MutableSet<OnStateChanged> = mutableSetOf()

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    private val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    init {
        builder()
        scope.launchEx {
            for (eventType in eventChannel) eventType.processEvent()
        }
    }

    private suspend fun @UnsafeVariance E.processEvent() {
        mapper.findStateForEvent(this, state, previousChanges)?.also { targetState ->
            previousStates.add(targetState)
            event?.let {
                previousChanges.add(
                    OnStateChanged(it, state, TimeEx())
                ).asUnit()
            }.asUnit()
        }
        processedEventCount.incrementAndGet()
        if (!isProcessingActive) scope.launch { finishedProcessingEvent.send(true) }
    }

    override suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }

    override fun release() = scope.cancel().asUnit()
}

data class OnStateChanged(
    val event: IEvent,
    val state: IState,
    val time: ITimeEx
)