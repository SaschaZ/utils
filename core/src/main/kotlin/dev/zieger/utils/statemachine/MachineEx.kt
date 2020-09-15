@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.TypeContinuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.MachineEx.Companion.DEFAULT_PREVIOUS_CHANGES_SIZE
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel
import dev.zieger.utils.statemachine.MachineEx.Companion.debugLevel
import dev.zieger.utils.statemachine.conditionelements.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
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
 * # **Define [Event]s, [State]s and [IData]**
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
 * Data classes need to implement the [IData] class:
 * ```
 * sealed class TestData : Data() {
 *
 *  data class TestEventData(val foo: String) : TestData()
 *  data class TestStateData(val moo: Boolean) : TestData()
 * }
 * ```
 *
 * # **[Event] conditions**
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
 *   -FIRST + INITIAL set A
 *
 *   // When the [SECOND] [Event] is received change [State] to [B].
 *   -SECOND set B
 *
 *   // When the current [State] is [A] or [B] and the [FIRST], [SECOND] or [THIRD] [Event] is received change the
 *   // [State] to [C].
 *   -FIRST + SECOND + THIRD + A + B set C
 *
 *   // When the current [State] is [C] and the {THIRD] [Event] is received execute the provided lambda and set the
 *   // new [State] to [D]-
 *   -THIRD + C execAndSet {
 *     // do something
 *     D // return the new state that should be activated for the provided event condition.
 *   }
 * }
 * ```
 *
 * You can see that event conditions must contain at least one [Event] and optional [State](s).
 * Because there can only be one incoming [Event] and one existing [State] only one [Event] or [State] of the
 * condition need to be equal. BUT keep in mind when providing no optional [State](s) the event condition will
 * react on all [State]s.
 *
 * # **[State] conditions**
 *
 * To react on [State]s when they get activated by an event condition you can use state conditions:
 *
 * ```
 *   // When [C] gets activated execute the provided lambda.
 *   -C exec {
 *       // do something
 *    } // [State] conditions can not set a new [State].
 *
 *   // When [C] gets activated with the incoming [Event] [FIRST] execute the provided lambda.
 *   -C + FIRST exec {
 *       // do something
 *    }
 * }
 * ```
 *
 * Summarize difference between [Event] and [State] conditions:
 * - First parameter of an [Event] condition must be an [Event] -> First parameter of a [State] condition must be
 *   a [State].
 * - [Event] conditions are triggered on incoming [Event]s -> [State] conditions are triggered by the any state
 *   that gets activated by an [Event] condition
 * - [State] conditions can not activate a new [State]. One [Event] condition per incoming [Event] can activate a
 *   new [State].
 * - [Event] conditions match against all [State]s when the condition does not contain [State] parameter ->
 *   [State] conditions match against all [Event]s when the condition does not contain an [Event] parameter
 *
 * # **[IData]**
 *
 * [IData] instances can be attached to any [Event] or [State] with the * operator.
 * Attached [IData] is than included when matching conditions.
 * ```
 * // When [State] is [C] and the incoming [Event] of [THIRD] has attached [TestEventData] with the content
 * "foo" the [State] [D] gets activated.
 * -THIRD * TestEventData("foo") + C execAndSet {
 *   eventData<TestEventData>().foo assert "foo" % "foo String is not \"foo\""
 *   D
 * }
 * ```
 * There are several methods and properties to access current and previous [Event]s, [State]s and [IData].
 * See [MatchScope] for more details.
 *
 * # **Exclude**
 *
 * You can also exclude specific [Event](s) or [State]s that must not be matching.
 * Also works with attached [IData].
 * ```
 * // When [State] [C] gets activated not by [Event] [FIRST] execute provided lambda.
 * -C - FIRST exec {
 *   // do something
 * }
 * ```
 *
 * # **Previous [Event]s and [State]s**
 * # **Applying [Event]s to the state machine**
 * # **Benefits of using operators for defining conditions**
 * # **Coroutine support**
 *
 *
 * @property initialState [State] that is activated initially in the state machine.
 * @property scope [CoroutineScopeEx] that is used for all Coroutine operations.
 * @property previousChangesCacheSize Size of the cache that store the state changes. Defaulting to
 * [DEFAULT_PREVIOUS_CHANGES_SIZE].
 * @property debugLevel [DebugLevel] for log messages used in the whole state machine.
 * @param builder Lambda that defines all state machine conditions. See [MachineDsl] for more details.
 */
open class MachineEx(
    private val initialState: State,
    val scope: CoroutineScopeEx = DefaultCoroutineScope(),
    private val previousChangesCacheSize: Int = DEFAULT_PREVIOUS_CHANGES_SIZE,
    debugLevel: DebugLevel = DebugLevel.ERROR,
    builder: suspend MachineDsl.() -> Unit
) : MachineDsl() {

    companion object {

        const val DEFAULT_PREVIOUS_CHANGES_SIZE = 128

        @Suppress("unused")
        enum class DebugLevel {
            ERROR,
            INFO,
            DEBUG
        }

        internal var debugLevel: DebugLevel = DebugLevel.ERROR
            private set
    }

    private val previousChanges = FiFo<OnStateChanged>(previousChangesCacheSize)

    private val finishedProcessingEvent = Channel<Boolean>()

    private val eventChannel = Channel<Pair<EventCombo, (StateCombo) -> Unit>>(Channel.RENDEZVOUS)

    private lateinit var eventCombo: EventCombo
    override val event: Event get() = eventCombo.master
    override val eventData: Data? get() = eventCombo.slave as? Data

    override suspend fun setEventSync(event: EventCombo): StateCombo {
        val cont = TypeContinuation<StateCombo>()
        eventChannel.send(event to { state -> cont.trigger(state) })
        return cont.suspendUntilTrigger()
    }

    override fun fireAndForget(event: EventCombo) = scope.launchEx { setEventSync(event) }.asUnit()

    private var stateCombo: StateCombo = initialState.combo
    override val state: State get() = stateCombo.master
    override val stateData: Data? get() = stateCombo.slave as? Data

    private val submittedEventCount = AtomicInteger(0)
    private val processedEventCount = AtomicInteger(0)
    private val isProcessingActive: Boolean
        get() = submittedEventCount.get() != processedEventCount.get()

    init {
        MachineEx.debugLevel = debugLevel

        scope.launchEx {
            applyNewState(initialState.combo to suspend {}, initialState.combo, object : EventImpl() {
                override val noLogging: Boolean
                    get() = false
            }.combo)

            this@MachineEx.builder()
            for (event in eventChannel) event.processEvent()
        }
    }

    private suspend fun Pair<EventCombo, (StateCombo) -> Unit>.processEvent() {
        eventCombo = first
        val stateBefore = stateCombo

        applyNewState(mapper.processEvent(eventCombo, stateBefore, previousChanges.reversed()), stateBefore, eventCombo)
        processedEventCount.incrementAndGet()

        if (!isProcessingActive)
            scope.launch { finishedProcessingEvent.send(true) }

        second(stateCombo)
    }

    private suspend fun applyNewState(
        newState: Pair<StateCombo, suspend () -> Unit>?,
        stateBefore: StateCombo,
        event: EventCombo
    ) = newState?.let {
        stateCombo = newState.first
        previousChanges.put(OnStateChanged(event, stateBefore, newState.first).apply {
            stateBefore.run { activeStateChanged(false) }
            event.run { fired() }
            newState.first.run { activeStateChanged(true) }
        })
        newState.second()
    }

    override suspend fun suspendUtilProcessingFinished() {
        while (isProcessingActive) finishedProcessingEvent.receive()
    }

    override fun clearPreviousChanges() = previousChanges.clear()

    override fun release() = scope.cancel().asUnit()
}