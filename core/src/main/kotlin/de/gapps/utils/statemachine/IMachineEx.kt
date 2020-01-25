package de.gapps.utils.statemachine

import de.gapps.utils.delegates.IOnChangedScope

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
     * Get latest event or set a new event.
     */
    var event: @UnsafeVariance E

    /**
     * Returns current state.
     */
    val state: S

    /**
     * Observe any event change.
     *
     * @param observer Is notified when an event changed.
     * @return Invoke to remove the observer.
     */
    fun observeEvent(observer: IOnChangedScope<IMachineEx<@UnsafeVariance E, @UnsafeVariance S>, E>.(E) -> Unit): () -> Unit

    /**
     * Observe any state change.
     *
     * @param observer Is notified when an state changed.
     * @return Invoke to remove the observer.
     */
    fun observeState(observer: IOnChangedScope<IMachineEx<@UnsafeVariance E, @UnsafeVariance S>, S>.(S) -> Unit): () -> Unit
}