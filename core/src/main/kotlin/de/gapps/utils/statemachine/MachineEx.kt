@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.scopes.IMachineExScope
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
 * Base interface for a state of [MachineEx]
 */
interface IState

/**
 * TODO
 */
interface IMachineEx<out EventType : IEvent, out StateType : IState> {

    val machineExScope: IMachineExScope<EventType, StateType>

    var event: @UnsafeVariance EventType?
    val previousEvents: List<EventType>

    val state: StateType
    val previousStates: List<StateType>

    val findStateForEvent: suspend IMachineEx<@UnsafeVariance EventType, @UnsafeVariance StateType>.(event: @UnsafeVariance EventType) -> StateType?

    val previousChanges: List<Pair<@UnsafeVariance EventType, @UnsafeVariance StateType>>

    val isProcessingActive: Boolean

    suspend fun suspendUtilProcessingFinished()

    fun release()
}

/**
 * TODO
 */
open class MachineEx<out EventType : IEvent, out StateType : IState>(
    private val initialState: StateType,
    override val machineExScope: IMachineExScope<EventType, StateType>,
    protected val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    override val findStateForEvent: suspend IMachineEx<@UnsafeVariance EventType, @UnsafeVariance StateType>.
        (event: @UnsafeVariance EventType) -> StateType? = { null }
) : IMachineEx<EventType, StateType> {

    private val eventMutex = Mutex()
    private val eventChannel = Channel<EventType>(Channel.BUFFERED)
    override var event: @UnsafeVariance EventType?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()
    override val previousEvents: List<EventType>
        get() = mutablePreviousEvents
    private val mutablePreviousEvents = ArrayList<EventType>()

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    override val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    override val state: StateType
        get() = previousStates.lastOrNull() ?: initialState
    override val previousStates: List<StateType>
        get() = mutablePreviousStates
    private val mutablePreviousStates = ArrayList<StateType>()

    override val previousChanges: List<Pair<@UnsafeVariance EventType, @UnsafeVariance StateType>>
        get() = mutablePreviousStateChanges
    private val mutablePreviousStateChanges =
        ArrayList<Pair<@UnsafeVariance EventType, @UnsafeVariance StateType>>()

    private val finishedProcessingEvent = Channel<Boolean>()

    init {
        scope.launchEx {
            for (eventType in eventChannel) eventType.processEvent()
        }
    }

    private suspend fun @UnsafeVariance EventType.processEvent() {
        findStateForEvent(this)?.also { targetState ->
            mutablePreviousStates.add(targetState)
            event?.let {
                mutablePreviousStateChanges.add(
                    Pair(it, state)
                ).asUnit()
            }
        }
        processedEventCount.incrementAndGet()
        if (!isProcessingActive) scope.launch { finishedProcessingEvent.send(true) }
    }

    override suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }

    override fun release() = scope.cancel().asUnit()
}