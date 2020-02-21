@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface IEventScope<out E : IEvent<*, *>, out S : IState> : IOnScope<E, S> {
    val events: List<@UnsafeVariance E>
    val event
        get() = events.first()
}

class EventScope<out E : IEvent<*, *>, out S : IState>(
    onScope: IOnScope<E, S>,
    override val events: List<@UnsafeVariance E>
) : IEventScope<E, S>, IOnScope<E, S> by onScope {

    constructor(onScope: IOnScope<E, S>, event: E) : this(onScope, listOf(event))
}

interface IEventStateScope<out E : IEvent<*, *>, out S : IState> : IEventScope<E, S> {
    val states: List<@UnsafeVariance S>
    val state: S?
}

class EventStateScope<out E : IEvent<*, *>, out S : IState>(
    eventScope: IEventScope<E, S>,
    override val states: List<@UnsafeVariance S>
) : IEventStateScope<E, S>, IEventScope<E, S> by eventScope {

    constructor(eventScope: IEventScope<E, S>, state: @UnsafeVariance S) : this(eventScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}