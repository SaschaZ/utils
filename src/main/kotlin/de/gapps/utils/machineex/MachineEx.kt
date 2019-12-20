package de.gapps.utils.machineex

import de.gapps.utils.machineex.scopes.*
import de.gapps.utils.misc.Log
import de.gapps.utils.misc.ifN
import de.gapps.utils.misc.lastOrNull
import de.gapps.utils.observable.ChangeObserver
import de.gapps.utils.observable.Observable
import de.gapps.utils.observable.ObservedChangedScope

/**
 * Simple state machine.
 *
 * @param initialState The state machine is initialized with this state. This will not trigger any actions.
 * @property eventActionMapping Callback that is used to get the new state when an event comes in.
 */
open class MachineEx<out E : IEvent, out S : IState>(
    initialState: S,
    private val eventActionMapping: EventStateActionMapper<E, S>
) : IMachineEx<E, S> {

    private val recentChanges = ArrayList<EventChangeScope<E, S>>()

    private val eventHost = Observable<E?>(null) {
        it!!.processEvent().processState()
    }
    override var event: @UnsafeVariance E
        get() = eventHost.value ?: throw IllegalStateException("Can not provide unset event")
        set(value) {
            eventHost.value = value
        }

    val set: ISetScope<@UnsafeVariance E, @UnsafeVariance S>
        get() = SetScope(eventHost)

    private var stateHost = Observable(initialState)

    override val state: S
        get() = stateHost.value

    override fun observeEvent(observer: ChangeObserver<E>) = eventHost.observe {
        value?.let { ObservedChangedScope(it, previousValue, previousValues.filterNotNull()) }
    }

    override fun observeState(observer: ChangeObserver<S>) = stateHost.observe(observer)

    protected open fun @UnsafeVariance E.processEvent(): S =
        OnEventScope(this, state, recentChanges).run { eventActionMapping(this@processEvent) }

    protected open fun @UnsafeVariance S.processState() {
        stateHost.value = this
    }
}

fun <E : IEvent, S : IState> machineEx(
    initialState: S,
    builder: MachineExScope<E, S>.() -> Unit
): MachineEx<E, S> {
    return MachineExScope<E, S>().run {
        builder()
        MachineEx<E, S>(initialState) { e ->
            EventChangeScope(
                event,
                previousChanges.lastOrNull()?.stateBefore,
                state
            ).run {
                eventStatesActionMapping.entries.firstOrNull { it.key.first == e && it.key.second == state }
                    ?.value?.invoke(this) ifN {
                    Log.w("No state defined for event $event with state $state.")
                    state
                }
            }
        }.also { it.observeState { s -> stateEventStateActionMap[s]?.invoke(StateChangeScope(previousValue, value)) } }
    }
}

typealias EventStateActionMapper<E, S> = IOnEventScope<E, S>.(E) -> S