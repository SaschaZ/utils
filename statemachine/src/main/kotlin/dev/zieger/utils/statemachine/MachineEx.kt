@file:Suppress("MemberVisibilityCanBePrivate", "LeakingThis")

package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.coroutines.suspendCoroutine
import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.MachineEx.Companion.DEFAULT_PREVIOUS_CHANGES_SIZE
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.dsl.MachineDsl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel


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
 * # **Define [AbsEvent]s, [AbsState]s and [IData]**
 *
 * States need to implement the [AbsState] class. :
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
 * Events need to implement the [AbsEvent] class:
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
 * # **[AbsEvent] conditions**
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
 * You can see that event conditions must contain at least one [AbsEvent] and optional [State](s).
 * Because there can only be one incoming [AbsEvent] and one existing [AbsState] only one [AbsEvent] or [AbsState] of the
 * condition need to be equal. BUT keep in mind when providing no optional [State](s) the event condition will
 * react on all [AbsState]s.
 *
 * # **[AbsState] conditions**
 *
 * To react on [AbsState]s when they get activated by an event condition you can use state conditions:
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
 * Summarize difference between [AbsEvent] and [AbsState] conditions:
 * - First parameter of an [AbsEvent] condition must be an [AbsEvent] -> First parameter of a [AbsState] condition must be
 *   a [AbsState].
 * - [AbsEvent] conditions are triggered on incoming [AbsEvent]s -> [AbsState] conditions are triggered by the any state
 *   that gets activated by an [AbsEvent] condition
 * - [AbsState] conditions can not activate a new [AbsState]. One [AbsEvent] condition per incoming [AbsEvent] can activate a
 *   new [AbsState].
 * - [AbsEvent] conditions match against all [AbsState]s when the condition does not contain [AbsState] parameter ->
 *   [AbsState] conditions match against all [AbsEvent]s when the condition does not contain an [AbsEvent] parameter
 *
 * # **[IData]**
 *
 * [IData] instances can be attached to any [AbsEvent] or [AbsState] with the * operator.
 * Attached [IData] is than included when matching conditions.
 * ```
 * // When [State] is [C] and the incoming [Event] of [THIRD] has attached [TestEventData] with the content
 * "foo" the [State] [D] gets activated.
 * -THIRD * TestEventData("foo") + C execAndSet {
 *   eventData<TestEventData>().foo assert "foo" % "foo String is not \"foo\""
 *   D
 * }
 * ```
 * There are several methods and properties to access current and previous [AbsEvent]s, [AbsState]s and [IData].
 * See [MatchScope] for more details.
 *
 * # **Exclude**
 *
 * You can also exclude specific [Event](s) or [AbsState]s that must not be matching.
 * Also works with attached [IData].
 * ```
 * // When [State] [C] gets activated not by [Event] [FIRST] execute provided lambda.
 * -C - FIRST exec {
 *   // do something
 * }
 * ```
 *
 * # **Previous [AbsEvent]s and [AbsState]s**
 * # **Applying [AbsEvent]s to the state machine**
 * # **Benefits of using operators for defining conditions**
 * # **Coroutine support**
 *
 *
 * @param initialState [AbsState] that is activated initially in the state machine.
 * @property scope [CoroutineScopeEx] that is used for all Coroutine operations.
 * @param previousChangesCacheSize Size of the cache that store the state changes. Defaulting to
 * [DEFAULT_PREVIOUS_CHANGES_SIZE].
 * @property debugLevel [DebugLevel] for log messages used in the whole state machine.
 * @param builder Lambda that defines all state machine conditions. See [MachineOperatorDsl] for more details.
 */
open class MachineEx(
    initialState: AbsState,
    val scope: CoroutineScope,
    previousChangesCacheSize: Int = DEFAULT_PREVIOUS_CHANGES_SIZE,
    debugLevel: DebugLevel = DebugLevel.ERROR,
    logScope: ILogScope = LogScope.copy {
        logLevel = when (debugLevel) {
            DebugLevel.DEBUG -> LogLevel.VERBOSE
            DebugLevel.INFO -> LogLevel.INFO
            DebugLevel.ERROR -> LogLevel.EXCEPTION
        }
    },
    builder: suspend MachineDsl.() -> Unit
) : MachineDsl, IMachineEx {

    companion object {

        private const val DEFAULT_PREVIOUS_CHANGES_SIZE = 128

        @Suppress("unused")
        enum class DebugLevel {
            DEBUG,
            INFO,
            ERROR
        }
    }

    override val processor: IMachineExProcessor = MachineExProcessor(logScope)

    private val previousChanges = FiFo<OnStateChanged>(previousChangesCacheSize)

    private val eventChannel = Channel<Pair<EventCombo, (StateCombo) -> Unit>>(Channel.RENDEZVOUS)

    private var eventCombo: EventCombo = Event().combo
    override val event: AbsEvent get() = eventCombo.master
    override val eventData: Data? get() = eventCombo.slave as? Data

    private var stateCombo: StateCombo = initialState.combo
    override val state: AbsState get() = stateCombo.master
    override val stateData: Data? get() = stateCombo.slave as? Data

    init {
        scope.launchEx {
            this@MachineEx.builder()
            for (event in eventChannel) event.processEvent()
        }
    }

    override fun fireAndForget(event: EventCombo): Job = scope.launchEx { fire(event) }

    override suspend fun fire(event: EventCombo): StateCombo = suspendCoroutine {
        eventChannel.send(event to { state -> resume(state) })
    }

    private suspend fun Pair<EventCombo, (StateCombo) -> Unit>.processEvent() {
        eventCombo = first

        processor.processEvent(eventCombo, stateCombo, previousChanges.reversed())?.also { newState ->
            previousChanges.put(OnStateChanged(eventCombo, stateCombo, newState))
            stateCombo = newState

            processor.processState(eventCombo, stateCombo, previousChanges.reversed())?.also { newEvent ->
                (newEvent to { sc: StateCombo -> second(sc) }).processEvent()
            } ?: second(stateCombo)
        } ?: second(stateCombo)
    }

    override fun clearPreviousChanges() = previousChanges.clear()

    override fun release() = scope.cancel().asUnit()
}