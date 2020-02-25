package de.gapps.utils.statemachine.scopes.lvl0

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope

interface IOnEventScope<out E : IEvent, out S : IState> {

    val event: E
    val state: S

    val previousChanges: List<EventChangeScope<E, S>>
}

data class OnEventScope<out E : IEvent, out S : IState>(
    override val event: E,
    override val state: S,
    override val previousChanges: List<EventChangeScope<E, S>>
) : IOnEventScope<E, S>