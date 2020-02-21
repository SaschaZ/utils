package de.gapps.utils.statemachine

import de.gapps.utils.observable.Controller
import de.gapps.utils.statemachine.scopes.ISetScope

/**
 * Base interface for an event of [IMachineEx]
 */
interface IEvent<out K : Any, out V : Any> : MutableMap<@UnsafeVariance K, @UnsafeVariance V>

open class Event<out K : Any, out V : Any> : IEvent<K, V>, MutableMap<@UnsafeVariance K, @UnsafeVariance V> by HashMap()

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
interface IMachineEx<out K : Any, out V : Any, out E : IEvent<K, V>, out S : IState> {

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
    val set: ISetScope<K, V, @UnsafeVariance E>

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