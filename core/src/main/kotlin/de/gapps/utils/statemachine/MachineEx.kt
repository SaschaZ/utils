@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.scopes.lvl0.IOnEventScope
import de.gapps.utils.statemachine.scopes.lvl0.OnEventScope
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base interface for an event of [IMachineEx]
 */
interface IEvent : MutableMap<String, String>

open class Event : IEvent, MutableMap<String, String> by HashMap()

/**
 * Base interface for a state of [IMachineEx]
 */
interface IState

open class MachineEx<out EventType : IEvent, out StateType : IState>(
    private val initialState: StateType,
    protected val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    val findStateForEvent: IOnEventScope<@UnsafeVariance EventType,
            @UnsafeVariance StateType>.(event: @UnsafeVariance EventType) -> StateType? = { null }
) {
    private val eventMutex = Mutex()
    private val eventChannel = Channel<EventType>(Channel.BUFFERED)
    var event: @UnsafeVariance EventType?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()
    val previousEvents: List<EventType>
        get() = mutablePreviousEvents
    private val mutablePreviousEvents = ArrayList<EventType>()

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    val state: StateType
        get() = previousStates.lastOrNull() ?: initialState
    val previousStates: List<StateType>
        get() = mutablePreviousStates
    private val mutablePreviousStates = ArrayList<StateType>()

    val previousChanges: List<EventChangeScope<@UnsafeVariance EventType, @UnsafeVariance StateType>>
        get() = mutablePreviousStateChanges
    private val mutablePreviousStateChanges =
        ArrayList<EventChangeScope<@UnsafeVariance EventType, @UnsafeVariance StateType>>()

    init {
        scope.launchEx {
            for (eventType in eventChannel) eventType.processEvent()
        }
    }

    private fun @UnsafeVariance EventType.processEvent() {
        OnEventScope(this, state, previousChanges).findStateForEvent(this)?.also { targetState ->
            mutablePreviousStates.add(targetState)
            event?.let { mutablePreviousStateChanges.add(EventChangeScope(it, state)).asUnit() }
        }
        processedEventCount.incrementAndGet()
        if (!isProcessingActive) scope.launch { finishedProcessingEvent.send(true) }
    }

    private val finishedProcessingEvent = Channel<Boolean>()

    open fun release() = scope.cancel().asUnit()

    suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }
}