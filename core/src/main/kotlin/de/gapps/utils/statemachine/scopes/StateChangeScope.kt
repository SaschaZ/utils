package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IState


interface IStateChangeScope<S : IState> {
    val state: S
}

data class StateChangeScope<S : IState>(
    override val state: S
) : IStateChangeScope<S>