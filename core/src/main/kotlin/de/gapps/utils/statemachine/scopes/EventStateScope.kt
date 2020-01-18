package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface IEventStateScope<out E : IEvent, out S : IState> : IEventScope<E> {
    val states: List<S>
    val state
        get() = states.first()
}

class EventStateScope<out E : IEvent, out S : IState>(
    events: List<E>,
    override val states: List<S>
) : IEventStateScope<E, S>, IEventScope<E> by EventScope(events) {
    constructor(events: List<E>, state: S) : this(events, listOf(state))
}

