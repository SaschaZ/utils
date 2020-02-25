package de.gapps.utils.statemachine.scopes.lvl2

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl1.IEventScope
import de.gapps.utils.statemachine.scopes.lvl4.EventStateScope

infix fun <E : IEvent, S : IState> IEventScope<@UnsafeVariance E, @UnsafeVariance S>.withState(state: @UnsafeVariance S) =
    EventStateScope(this, state)

infix fun <E : IEvent, S : IState> IEventScope<@UnsafeVariance E, @UnsafeVariance S>.withStates(states: List<@UnsafeVariance S>) =
    EventStateScope(this, states)