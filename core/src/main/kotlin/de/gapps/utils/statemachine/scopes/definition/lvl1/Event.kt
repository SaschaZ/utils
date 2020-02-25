@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes.definition.lvl1

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.IEventHolder
import de.gapps.utils.statemachine.scopes.definition.lvl0.IOnScope
import de.gapps.utils.statemachine.scopes.lvl4.EventStateScope
import de.gapps.utils.statemachine.scopes.lvl4.EventTypeStateScope
import kotlin.reflect.KClass


interface IEventScope<out E : IEvent, out S : IState> :
    IOnScope<E, S>, IEventHolder<E> {

    infix fun withStates(states: List<@UnsafeVariance S>) = EventStateScope(this, states)
}

class EventScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val events: List<@UnsafeVariance E>
) : IEventScope<E, S>, IOnScope<E, S> by onScope

interface IEventTypeScope<out E : IEvent, out S : IState> :
    IOnScope<E, S> {
    val eventTypes: List<KClass<@UnsafeVariance E>>
    val event
        get() = eventTypes.first()

    infix fun withStates(states: List<@UnsafeVariance S>) = EventTypeStateScope<E, S>(this, states.map {
        @Suppress("UNCHECKED_CAST")
        it::class as KClass<S>
    })
}

class EventTypeScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val eventTypes: List<KClass<@UnsafeVariance E>>
) : IEventTypeScope<E, S>, IOnScope<E, S> by onScope

interface IStateChangeScope<out S : IState> {
    val state: S
}

data class StateChangeScope<out S : IState>(
    override val state: S
) : IStateChangeScope<S>