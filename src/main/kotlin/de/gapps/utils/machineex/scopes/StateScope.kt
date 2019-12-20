package de.gapps.utils.machineex.scopes

import de.gapps.utils.machineex.IState


interface IStateScope<out S : IState> {
    val states: List<S>
    val state
        get() = states.first()
}

class StateScope<out S : IState>(override val states: List<S>) : IStateScope<S> {
    constructor(state: S) : this(listOf(state))
}