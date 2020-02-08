package de.gapps.utils.statemachine

import de.gapps.utils.observable.Controller
import de.gapps.utils.statemachine.scopes.ISetScope

/**
 * Base interface for an event of [IMachineEx]
 */
interface IEvent

/**
 * Base interface for a state of [IMachineEx]
 */
interface IState

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
     * Get latest event.
     */
    val event: @UnsafeVariance E

    /**
     * Returns current state.
     */
    val state: S

    /**
     * Returns a scope which should be used to fire the next event.
     */
    val set: ISetScope<@UnsafeVariance E>

    /**
     * Observe any event change.
     *
     * @param observer Is notified when an event changed.
     * @return Invoke to remove the observer.
     */
    fun observeEvent(observer: Controller<@UnsafeVariance S>): () -> Unit

    /**
     * Observe any state change.
     *
     * @param observer Is notified when an state changed.
     * @return Invoke to remove the observer.
     */
    fun observeState(observer: Controller<@UnsafeVariance S>): () -> Unit
}