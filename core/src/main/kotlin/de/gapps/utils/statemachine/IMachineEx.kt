package de.gapps.utils.statemachine

import de.gapps.utils.delegates.IOnChangedScope
import de.gapps.utils.observable.Controller2
import de.gapps.utils.statemachine.scopes.ISetScope
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx

/**
 * Base interface for an event of [IMachineEx]
 */
interface IEvent {
    val fireAt: ITimeEx
}

open class Event : IEvent {
    override var fireAt: ITimeEx = TimeEx()
}

/**
 * Base interface for a state of [IMachineEx]
 */
interface IState {
    val active: Boolean
}

open class State : IState {
    override var active: Boolean = false
}

/**
 * Base interface for a state machine.
 * Holds a state if type [IState] that can be changed by an event of type [IEvent].
 * Provides methods to observe state and event changes.
 *
 * @param E Event type of [IEvent].
 * @param S State type of [IState].
 */
interface IMachineEx<out E : IEvent, out S : IState> {

    /**
     * Get latest event or set a new event.
     */
    var event: @UnsafeVariance E

    /**
     * Returns current state.
     */
    val state: S

    val set: ISetScope<@UnsafeVariance E, @UnsafeVariance S>

    /**
     * Observe any event change.
     *
     * @param observer Is notified when an event changed.
     * @return Invoke to remove the observer.
     */
    fun observeEvent(observer: Controller2<@UnsafeVariance IMachineEx<E, S>, @UnsafeVariance S>): () -> Unit

    /**
     * Observe any state change.
     *
     * @param observer Is notified when an state changed.
     * @return Invoke to remove the observer.
     */
    fun observeState(observer: Controller2<@UnsafeVariance IMachineEx<E, S>, @UnsafeVariance S>): () -> Unit
}