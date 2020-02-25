package de.gapps.utils.statemachine.scopes.lvl4

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl1.IEventScope


interface IEventStateScope<out E : IEvent, out S : IState> :
    IEventScope<E, S> {
    val states: List<@UnsafeVariance S>
    val state: S?
}

class EventStateScope<out E : IEvent, out S : IState>(
    eventScope: IEventScope<E, S>,
    override val states: List<@UnsafeVariance S>
) : IEventStateScope<E, S>, IEventScope<E, S> by eventScope {

    constructor(eventScope: IEventScope<E, S>, state: @UnsafeVariance S) : this(eventScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}