@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.asUnit
import de.gapps.utils.statemachine.IConditionElement.IComboElement
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave.IData
import de.gapps.utils.statemachine.IConditionElement.UsedAs.RUNTIME
import de.gapps.utils.statemachine.MachineEx.Companion.DebugLevel
import de.gapps.utils.statemachine.MachineEx.Companion.debugLevel
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
 *
 * # **Usage**
 *
 * First you need to define the events, states and data classes that should be used in the state machine:
 * Because event and state instances are not allowed to change it makes sense to use objects for them. Any dynamic data
 * can be attached with a [IData] class.
 *
 * # **Define [IEvent]s, [IState]s and [IData]**
 *
 * States need to implement the [IState] class. :
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
 * Events need to implement the [IEvent] class:
 * ```
 * sealed class TestEvent(ignoreData: Boolean = false) : Event(ignoreData) {
 *
 *  object FIRST : TestEvent()
 *  object SECOND : TestEvent(true)
 *  object THIRD : TestEvent()
 *  object FOURTH : TestEvent()
 * }
 * ```
 * Data classes need to implement the [IData] class:
 * ```
 * sealed class TestData : Data() {
 *
 *  data class TestEventData(val foo: String) : TestData()
 *  data class TestStateData(val moo: Boolean) : TestData()
 * }
 * ```
 *
 * # **[IEvent] conditions**
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
 *   // When the current [IState] is [INITIAL] and the [FIRST] event is received change the [IState] to [A].
 *   -FIRST + INITIAL set A
 *
 *   // When the [SECOND] [IEvent] is received change [IState] to [B].
 *   -SECOND set B
 *
 *   // When the current [IState] is [A] or [B] and the [FIRST], [SECOND] or [THIRD] [IEvent] is received change the
 *   // [IState] to [C].
 *   -FIRST + SECOND + THIRD + A + B set C
 *
 *   // When the current [IState] is [C] and the {THIRD] [IEvent] is received execute the provided lambda and set the
 *   // new [IState] to [D]-
 *   -THIRD + C execAndSet {
 *     // do something
 *     D // return the new state that should be activated for the provided event condition.
 *   }
 * }
 * ```
 *
 * You can see that event conditions must contain at least one [IEvent] and optional [IState](s).
 * Because there can only be one incoming [IEvent] and one existing [IState] only one [IEvent] or [IState] of the
 * condition need to be equal. BUT keep in mind when providing no optional [IState](s) the event condition will
 * react on all [IState]s.
 *
 * # **[IState] conditions**
 *
 * To react on [IState]s when they get activated by an event condition you can use state conditions:
 *
 * ```
 *   // When [C] gets activated execute the provided lambda.
 *   -C exec {
 *       // do something
 *    } // [IState] conditions can not set a new [IState].
 *
 *   // When [C] gets activated with the incoming [IEvent] [FIRST] execute the provided lambda.
 *   -C + FIRST exec {
 *       // do something
 *    }
 * }
 * ```
 *
 * Summarize difference between [IEvent] and [IState] conditions:
 * - First parameter of an [IEvent] condition must be an [IEvent] -> First parameter of a [IState] condition must be
 *   a [IState].
 * - [IEvent] conditions are triggered on incoming [IEvent]s -> [IState] conditions are triggered by the any state
 *   that gets activated by an [IEvent] condition
 * - [IState] conditions can not activate a new [IState]. One [IEvent] condition per incoming [IEvent] can activate a
 *   new [IState].
 * - [IEvent] conditions match against all [IState]s when the condition does not contain [IState] parameter ->
 *   [IState] conditions match against all [IEvent]s when the condition does not contain an [IEvent] parameter
 *
 * # **[IData]**
 *
 * [IData] instances can be attached to any [IEvent] or [IState] with the * operator.
 * Attached [IData] is than included when matching conditions.
 * ```
 * // When [IState] is [C] and the incoming [IEvent] of [THIRD] has attached [TestEventData] with the content
 * "foo" the [IState] [D] gets activated.
 * -THIRD * TestEventData("foo") + C execAndSet {
 *   eventData<TestEventData>().foo onFail "foo String is not \"foo\"" assert "foo"
 *   D
 * }
 * ```
 * There are several methods and properties to access current and previous [IEvent]s, [IState]s and [IData].
 * See [ExecutorScope] for more details.
 *
 * # **Exclude**
 *
 * You can also exclude specific [IEvent](s) or [IState]s that must not be matching.
 * Also works with attached [IData].
 * ```
 * // When [IState] [C] gets activated not by [IEvent] [FIRST] execute provided lambda.
 * -C - FIRST exec {
 *   // do something
 * }
 * ```
 *
 * # **Previous [IEvent]s and [IState]s**
 * # **Applying [IEvent]s to the state machine**
 * # **Benefits of using operators for defining conditions**
 * # **Coroutine support**
 *
 *
 * @property initialState [IState] that is activated initially in the state machine.
 * @property scope [CoroutineScopeEx] that is used for all Coroutine operations.
 * @property previousChangesCacheSize Size of the cache that store the state changes.
 * @property debugLevel [DebugLevel] for log messages used in the whole state machine.
 * @param builder Lambda that defines all state machine conditions. See [MachineDsl] for more details.
 */
open class MachineEx(
    private val initialState: IState,
    override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    private val previousChangesCacheSize: Int = 128,
    debugLevel: DebugLevel = DebugLevel.ERROR,
    builder: suspend MachineDsl.() -> Unit
) : MachineDsl() {

    companion object {

        @Suppress("unused")
        enum class DebugLevel {
            ERROR,
            INFO,
            DEBUG
        }

        internal var debugLevel: DebugLevel = DebugLevel.ERROR
            private set
    }

    override val mapper: IMachineExMapper = MachineExMapper()

    private val previousChanges: MutableList<OnStateChanged> = ArrayList()

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventMutex = Mutex()
    private val eventChannel = Channel<IComboElement>(Channel.BUFFERED)

    override var event: IComboElement
        get() = currentEvent
        set(value) {
            submittedEventCount.incrementAndGet()
            scope.launchEx(mutex = eventMutex) { eventChannel.send(value) }
        }

    private lateinit var currentEvent: IComboElement

    override var state: IComboElement = initialState.combo

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    private val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    init {
        MachineEx.debugLevel = debugLevel
        scope.launchEx {
            this@MachineEx.builder()
            for (event in eventChannel) event.processEvent()
        }
    }

    private suspend fun IComboElement.processEvent() {
        currentEvent = this
        currentEvent.usedAs = RUNTIME
        val stateBefore = this@MachineEx.state
        applyNewState(mapper.findStateForEvent(this, stateBefore, previousChanges), stateBefore, this)
        processedEventCount.incrementAndGet()
        if (!isProcessingActive)
            scope.launch { finishedProcessingEvent.send(true) }
    }

    private fun applyNewState(
        newState: IComboElement?,
        stateBefore: IComboElement,
        event: IComboElement
    ) = newState?.let {
        state = newState
        previousChanges.add(
            OnStateChanged(event, stateBefore, newState).apply {
                stateBefore.state?.run { activeStateChanged(false) }
                event.event?.run { fired() }
                newState.state?.run { activeStateChanged(true) }
            }
        )
        if (previousChanges.size > previousChangesCacheSize)
            previousChanges.remove(previousChanges.first())
    }

    override suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }

    override fun clearPreviousChanges() = previousChanges.clear()

    override fun release() = scope.cancel().asUnit()
}