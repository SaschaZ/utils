@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base interface for an event of [MachineEx]
 */
interface IEvent {
    var data: IData?
}

open class EventImpl(override var data: IData? = null) : IEvent

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
open class MachineEx(
    private val initialState: IState,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    builder: IMachineEx.() -> Unit
) : IMachineEx {

    override val mapper: IMachineExMapper = MachineExMapper()

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventMutex = Mutex()
    private val eventChannel = Channel<IEvent>(Channel.BUFFERED)

    override var event: IEvent?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()

    override val previousEvents: MutableList<IEvent>  = ArrayList()

    override val state: IState
        get() = previousStates.lastOrNull() ?: initialState
    override val previousStates: MutableList<IState> = ArrayList()

    private val previousChanges: MutableSet<OnStateChanged> = mutableSetOf()

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    private val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    init {
        builder()
        scope.launchEx {
            for (event in eventChannel) event.processEvent()
        }
    }

    private suspend fun IEvent.processEvent() {
        mapper.findStateForEvent(this, state, previousChanges)?.also { targetState ->
            previousStates.add(targetState)
            event?.let {
                previousChanges.add(
                    OnStateChanged(it, state)
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
    val state: IState
)