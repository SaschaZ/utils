package de.gapps.utils.machineex.scopes

import de.gapps.utils.machineex.IState


interface IStateChangeScope<S : IState> {
    val stateBefore: S?
    val stateAfter: S
}

data class StateChangeScope<S : IState>(
    override val stateBefore: S?,
    override val stateAfter: S
) : IStateChangeScope<S>