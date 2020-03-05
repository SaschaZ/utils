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
interface IEvent<out D : Any> {
    var data: @UnsafeVariance D?
}

open class EventImpl<out D : Any>(override var data: @UnsafeVariance D? = null) : IEvent<D>

/**
 * Base interface for a state of [MachineEx]
 */
interface IState<out D : Any> {
    var data: @UnsafeVariance D?
}

open class StateImpl<out D : Any>(override var data: @UnsafeVariance D? = null) : IState<D>


/**
 * TODO
 */
open class MachineEx<out D : Any>(
    private val initialState: IState<D>,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    builder: IMachineEx<D>.() -> Unit
) : IMachineEx<D> {

    override val mapper: IMachineExMapper<D> = MachineExMapper()

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventMutex = Mutex()
    private val eventChannel = Channel<IEvent<D>>(Channel.BUFFERED)

    override var event: IEvent<@UnsafeVariance D>?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()

    override val previousEvents: MutableList<IEvent<@UnsafeVariance D>> = ArrayList()

    override val state: IState<D>
        get() = previousStates.lastOrNull() ?: initialState
    override val previousStates: MutableList<IState<@UnsafeVariance D>> = ArrayList()

    private val previousChanges: MutableSet<OnStateChanged<D>> = mutableSetOf()

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

    private suspend fun IEvent<D>.processEvent() {
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

data class OnStateChanged<out D : Any>(
    val event: IEvent<D>,
    val state: IState<D>
)