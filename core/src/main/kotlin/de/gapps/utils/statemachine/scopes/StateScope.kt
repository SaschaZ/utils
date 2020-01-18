package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IState


interface IStateScope<out S : IState> {
    val states: List<S>
    val state
        get() = states.first()
}

class StateScope<out S : IState>(override val states: List<S>) : IStateScope<S> {
    constructor(state: S) : this(listOf(state))
}