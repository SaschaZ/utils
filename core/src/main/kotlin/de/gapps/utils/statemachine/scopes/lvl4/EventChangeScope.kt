package de.gapps.utils.statemachine.scopes.lvl4

import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.lvl1.IStateChangeScope

interface IEventChangeScope<out E : IEvent, out S : IState> :
    IStateChangeScope<S> {
    val event: E
}

data class EventChangeScope<out E : IEvent, out S : IState>(
    override val event: E,
    override val state: S
) : IEventChangeScope<E, S> {
    override fun toString() = "${this::class.name}(event=$event, state=$state)"
}