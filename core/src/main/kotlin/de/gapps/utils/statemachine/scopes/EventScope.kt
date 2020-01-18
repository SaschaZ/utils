package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent

interface IEventScope<out E : IEvent> {
    val events: List<E>
    val event
        get() = events.first()
}

class EventScope<out E : IEvent>(
    override val events: List<E>
) : IEventScope<E> {
    constructor(event: E) : this(listOf(event))
}

