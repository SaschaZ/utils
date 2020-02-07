package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface IEventScope<out E : IEvent> : IOnScope {
    val events: List<@UnsafeVariance E>
    val event
        get() = events.first()
}

class EventScope<out E : IEvent>(
    onScope: IOnScope,
    override val events: List<@UnsafeVariance E>
) : IEventScope<E>, IOnScope by onScope {

    constructor(onScope: IOnScope, event: E) : this(onScope, listOf(event))
}

interface IEventStateScope<out E : IEvent, out S : IState> : IEventScope<E> {
    val states: List<@UnsafeVariance S>
    val state: S?
}

class EventStateScope<out E : IEvent, out S : IState>(
    eventScope: IEventScope<E>,
    override val states: List<@UnsafeVariance S>
) : IEventStateScope<E, S>, IEventScope<E> by eventScope {

    constructor(eventScope: IEventScope<E>, state: @UnsafeVariance S) : this(eventScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}