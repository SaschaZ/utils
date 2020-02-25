package de.gapps.utils.statemachine.scopes.lvl1

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl0.IOnScope

interface IStateScope<out E : IEvent, out S : IState> :
    IOnScope<E, S> {
    val states: List<S>
    val state: S?
}

class StateScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val states: List<S>
) : IStateScope<E, S>, IOnScope<E, S> by onScope {

    constructor(onScope: IOnScope<E, S>, state: S) : this(onScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}

infix fun <E : IEvent, S : IState> IOnScope<@UnsafeVariance E, @UnsafeVariance S>.state(state: @UnsafeVariance S) =
    StateScope(this, state)

infix fun <E : IEvent, S : IState> IOnScope<@UnsafeVariance E, @UnsafeVariance S>.states(states: List<@UnsafeVariance S>) =
    StateScope(this, states)