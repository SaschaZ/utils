@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.BaseType.State
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicInteger


/**
 * TODO
 */
open class MachineEx(
    private val initialState: State,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
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

    private val previousChanges: MutableSet<OnStateChanged> = mutableSetOf()

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
        mapper.findStateForEvent(this, stateBefore, previousChanges)?.also { targetState ->
            previousStates.add(targetState)
            previousChanges.add(OnStateChanged(this, stateBefore, targetState))
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
    val event: ValueDataHolder,
    val stateBefore: ValueDataHolder,
    val stateAfter: ValueDataHolder
)