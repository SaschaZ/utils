package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IState


interface IStateChangeScope<S : IState> {
    val stateBefore: S?
    val stateAfter: S
}

data class StateChangeScope<S : IState>(
    override val stateBefore: S?,
    override val stateAfter: S
) : IStateChangeScope<S>