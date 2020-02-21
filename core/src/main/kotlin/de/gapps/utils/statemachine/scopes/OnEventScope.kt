package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

interface IOnEventScope<E : IEvent<*, *>, S : IState> {

    val event: E
    val state: S

    val previousChanges: List<EventChangeScope<E, S>>
}

data class OnEventScope<E : IEvent<*, *>, S : IState>(
    override val event: E,
    override val state: S,
    override val previousChanges: List<EventChangeScope<E, S>>
) : IOnEventScope<E, S>