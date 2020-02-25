@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes.lvl1

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl0.IOnScope


interface IEventScope<out E : IEvent, out S : IState> :
    IOnScope<E, S> {
    val events: List<@UnsafeVariance E>
    val event
        get() = events.first()
}

class EventScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val events: List<@UnsafeVariance E>
) : IEventScope<E, S>, IOnScope<E, S> by onScope {

    constructor(onScope: IOnScope<E, S>, event: E) : this(onScope, listOf(event))
}

infix fun <E : IEvent, S : IState> IOnScope<@UnsafeVariance E, @UnsafeVariance S>.event(event: @UnsafeVariance E) =
    EventScope(this, event)

infix fun <E : IEvent, S : IState> IOnScope<@UnsafeVariance E, @UnsafeVariance S>.events(events: List<@UnsafeVariance E>) =
    EventScope(this, events)

interface IStateChangeScope<out S : IState> {
    val state: S
}

data class StateChangeScope<out S : IState>(
    override val state: S
) : IStateChangeScope<S>