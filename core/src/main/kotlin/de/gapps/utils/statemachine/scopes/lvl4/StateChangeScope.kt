package de.gapps.utils.statemachine.scopes.lvl4

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState


interface IStateChange<E : IEvent, S : IState> {

    val event: E
    val previousState: S
    val state: S
}

data class StateChange<E : IEvent, S : IState>(
    override val event: E,
    override val previousState: S,
    override val state: S
) : IStateChange<E, S>