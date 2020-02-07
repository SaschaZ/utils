package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IState

interface IStateScope<out S : IState> : IOnScope {
    val states: List<S>
    val state: S?
}

class StateScope<out S : IState>(
    onScope: IOnScope,
    override val states: List<S>
) : IStateScope<S>, IOnScope by onScope {

    constructor(onScope: IOnScope, state: S) : this(onScope, listOf(state))

    override val state: S?
        get() = states.firstOrNull()
}