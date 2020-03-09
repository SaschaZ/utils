@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.BaseType.Event
import de.gapps.utils.statemachine.BaseType.State
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger


/**
 * TODO
 *
 * @property initialState [State] that is activated initially in the state machine.
 * @property scope [CoroutineScopeEx] that is used for all Coroutine operations.
 * @property previousChangesCacheSize Size of the cache that store the state changes.
 * @param builder Lambda that defines all state machine conditions. See [IMachineOperators] for more details.
 */
open class MachineEx(
    private val initialState: State,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    private val previousChangesCacheSize: Int = 128,
    builder: suspend IMachineOperators.() -> Unit
) : IMachineEx, IMachineOperators {

    override val mapper: IMachineExMapper = MachineExMapper()

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventMutex = Mutex()
    private val eventChannel = Channel<ValueDataHolder>(Channel.BUFFERED)

    override var event: ValueDataHolder?
        get() = previousEvents.lastOrNull()
        set(value) = value?.also {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }.asUnit()

    override val previousEvents: MutableList<ValueDataHolder> = ArrayList()

    override val state: ValueDataHolder
        get() = previousStates.lastOrNull() ?: ValueDataHolder(initialState)
    override val previousStates: MutableList<ValueDataHolder> = ArrayList()

    private val previousChanges: MutableSet<OnStateChanged> = HashSet()

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    private val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    init {
        scope.launchEx {
            builder()
            for (event in eventChannel) event.processEvent()
        }
    }

    private suspend fun ValueDataHolder.processEvent() {
        val stateBefore = state
        mapper.findStateForEvent(this, stateBefore, previousChanges.toSet())?.also { targetState ->
            previousStates.add(targetState)
            applyNewState(targetState, state, this)
        }
        processedEventCount.incrementAndGet()
        if (!isProcessingActive)
            scope.launch { finishedProcessingEvent.send(true) }
    }

    private fun applyNewState(
        newState: ValueDataHolder?,
        state: ValueDataHolder,
        event: ValueDataHolder
    ) = newState?.let {
        val stickyStateData = state.data.filter { f -> f.isSticky }
        newState.data = setOf(*newState.data.toTypedArray(), *stickyStateData.toTypedArray())

        previousChanges.add(
            OnStateChanged(event, state, newState, previousChanges.take(previousChangesCacheSize).toSet()).apply {
                state.state<State>().run { activeStateChanged(false) }
                event.event<Event>().run { fired() }
                newState.state<State>().run { activeStateChanged(true) }
            }
        )
        if (previousChanges.size > previousChangesCacheSize)
            previousChanges.remove(previousChanges.first())
    }

    override suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }

    override fun release() = scope.cancel().asUnit()
}

