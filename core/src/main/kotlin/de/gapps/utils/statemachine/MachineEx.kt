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
 * State machine with the following features:
 * - maps actions to incoming events (event condition) and state changes (state condition)
 * - events and states can contain dynamic data instances that can be referenced in any condition
 * - DSL for defining event and state conditions
 * - full Coroutine support
 *
 * Usage:
 * First you need to define the events, states and data classes that should be used in the state machine:
 * Because event and state instances are not allowed to change it makes sense to use objects for them. Any dynamic data
 * can be attached with a [Data] class.
 *
 * States need to implement the [State] class. :
 * ```
 * sealed class TestState : State() {
 *
 *  object INITIAL : TestState()
 *  object A : TestState()
 *  object B : TestState()
 *  object C : TestState()
 *  object D : TestState()
 * }
 * ```
 * Events need to implement the [Event] class:
 * ```
 * sealed class TestEvent(ignoreData: Boolean = false) : Event(ignoreData) {
 *
 *  object FIRST : TestEvent()
 *  object SECOND : TestEvent(true)
 *  object THIRD : TestEvent()
 *  object FOURTH : TestEvent()
 * }
 * ```
 * Data classes need to implement the [Data] class:
 * ```
 * sealed class TestData : Data() {
 *
 *  data class TestEventData(val foo: String) : TestData()
 *  data class TestStateData(val moo: Boolean) : TestData()
 * }
 * ```
 *
 * After you have defined the needed events, states and possible data you can start defining the event and state
 * conditions:
 * ```
 * MachineEx(INITIAL) {
 *   // All conditions start with a - (minus) sign.
 *   // If the first parameter is an event the condition becomes an event condition.
 *   // If the first parameter is a state the condition becomes a state condition.
 *   // Some examples for event conditions:
 *
 *   // When the current [State] is [INITIAL] and the [FIRST] event is received change the [State] to [A].
 *   -FIRST + INITIAL += A
 *
 *   // When the [SECOND] [Event] is received change [State] to [B].
 *   -SECOND += B
 *
 *   // When the current [State] is [A] or [B] and the [FIRST], [SECOND] or [THIRD] [Event] is received change the
 *   // [State] to [C].
 *   -FIRST + SECOND + THIRD + A + B += C
 *
 *   // When the current [State] is [C] and the {THIRD] [Event] is received execute the provided lambda and set the
 *   // new [State] to [D]-
 *   -THIRD + C *= {
 *     // do something
 *     D // return the new state that should be activated for the provided event condition.
 *   }
 *
 *   // You can see that event conditions must contain at least one [Event] and optional [State](s).
 *   // Because there can only be one incoming [Event] and one existing [State] only one [Event] or [State] of the
 *   // condition need to be equal. BUT keep in mind when providing no optional [State](s) the event condition will
 *   // react on all [State]s.
 *
 *   // To react on [State]s when they get activated by an event condition you can use state conditions:
 *
 *   // When [C] gets activated execute the provided lambda.
 *   -C -= {
 *       // do something
 *    } // [State] conditions can not set a new [State].
 *
 *   // When [C] gets activated with the incoming [Event] [FIRST] execute the provided lambda.
 *   -C + FIRST -= {
 *       // do something
 *    }
 *
 *   // Summarize difference between [Event] and [State] conditions:
 *   // - First parameter of an [Event] condition must be an [Event] -> First parameter of a [State] condition must be
 *   //   a [State].
 *   // - [Event] conditions are triggered on incoming [Event]s -> [State] conditions are triggered by the any state
 *   //   that gets activated by an [Event] condition
 *   // - [State] conditions can not activate a new [State]. One [Event] condition per incoming [Event] can activate a
 *   //   new [State].
 *   // - [Event] conditions match against all [State]s when the condition does not contain [State] parameter ->
 *   //   [State] conditions match against all [Event]s when the condition does not contain an [Event] parameter
 * }
 * ```
 * TODO [Data] usage
 *
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