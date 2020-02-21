package de.gapps.utils.statemachine.scopes

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

interface IEventChangeScope<E : IEvent<*, *>, S : IState> : IStateChangeScope<S> {
    val event: E
}

data class EventChangeScope<E : IEvent<*, *>, S : IState>(
    override val event: E,
    override val state: S
) : IEventChangeScope<E, S> {
    override fun toString() = "${this::class.name}(event=$event, state=$state)"
}

interface IStateChange<E : IEvent<*, *>, S : IState> {

    val event: E
    val previousState: S
    val state: S
}

data class StateChange<E : IEvent<*, *>, S : IState>(
    override val event: E,
    override val previousState: S,
    override val state: S
) : IStateChange<E, S>