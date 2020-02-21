package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

interface IStateScope<out E : IEvent<*, *>, out S : IState> : IOnScope<E, S> {
    val states: List<S>
    val state: S?
}

class StateScope<out E : IEvent<*, *>, out S : IState>(
    onScope: IOnScope<E, S>,
    override val states: List<S>
) : IStateScope<E, S>, IOnScope<E, S> by onScope {

    constructor(onScope: IOnScope<E, S>, state: S) : this(onScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}