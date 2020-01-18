package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

interface IEventChangeScope<E : IEvent, S : IState> : IStateChangeScope<S> {
    val event: E
}

data class EventChangeScope<E : IEvent, S : IState>(
    override val event: E,
    override val stateBefore: S?,
    override val stateAfter: S
) : IEventChangeScope<E, S>