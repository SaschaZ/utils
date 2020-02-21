package de.gapps.utils.statemachine

import de.gapps.utils.delegates.OnChangedScope
import de.gapps.utils.log.Log
import de.gapps.utils.log.logV
import de.gapps.utils.misc.ifN
import de.gapps.utils.observable.Controllable
import de.gapps.utils.observable.Controller
import de.gapps.utils.observable.IControllable
import de.gapps.utils.statemachine.scopes.*

/**
 * Simple state machine.
 *
 *
 *
 * @param initialState The state machine is initialized with this state. This will not trigger any actions.
 * @property eventActionMapping Callback that is used to get the new state when an event comes in.
 */
open class MachineEx<out E : IEvent, out S : IState>(
    initialState: S,
    private val eventActionMapping: IOnEventScope<E, S>.(E) -> S
) : IMachineEx<E, S> {

    @Suppress("ClassName")
    object INITIAL_EVENT : IEvent, MutableMap<String, Any> by HashMap()

    private val recentChanges = ArrayList<EventChangeScope<E, S>>()

    @Suppress("UNCHECKED_CAST")
    private val eventHost: IControllable<E> = Controllable(INITIAL_EVENT as E) {
        it.processEvent().processState()
    }

    override var event: @UnsafeVariance E by eventHost

    override val set: ISetScope<@UnsafeVariance E>
        get() = SetScope(eventHost)

    private var stateHost = Controllable<S>(initialState)
    override val state: S by stateHost

    protected open fun @UnsafeVariance E.processEvent(): S =
        OnEventScope(this, state, recentChanges).run { eventActionMapping(this@processEvent) }

    protected open fun @UnsafeVariance S.processState() {
        stateHost.value = this
    }

    override fun observeEvent(observer: Controller<@UnsafeVariance S>) =
        eventHost.control {
            OnChangedScope(
                value,
                thisRef,
                previousValue,
                previousValues.filterNotNull(),
                { })
        }

    override fun observeState(observer: Controller<@UnsafeVariance S>) =
        stateHost.control(observer)
}

/**
 * Builder for MachineEx. Allows building of event action mapping with a simple DSL instead of providing it in a List.
 */
inline fun <reified E : IEvent, reified S : IState> machineEx(
    initialState: S,
    checkEventForType: Boolean = false,
    checkStateForType: Boolean = false,
    builder: MachineExScope<E, S>.() -> Unit
): IMachineEx<E, S> {
    return MachineExScope<E, S>().run {
        builder()
        MachineEx(initialState) { e ->
            EventChangeScope(event, state).run rt@{
                val fittingPair = eventStateMapping.entries.firstOrNull {
                    it.key.run {
                        if (checkEventForType) e::class.isInstance(first) else e == first
                                && if (checkStateForType) state::class.isInstance(second) else state == second
                    }
                }
                fittingPair?.value?.invoke(this) logV { "$this -> $it" } ifN {
                    Log.w("No state defined for event $event with state $state.")
                    state
                }
            }
        }
    }
}