package de.gapps.utils.statemachine.scopes.definition.lvl1

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope
import de.gapps.utils.statemachine.scopes.definition.IStateHolder
import de.gapps.utils.statemachine.scopes.definition.IStateMachineHolder
import de.gapps.utils.statemachine.scopes.definition.lvl0.IOnScope

interface IStateScope<out E : IEvent, out S : IState> :
    IOnScope<E, S>, IStateHolder<S>, IStateMachineHolder<E, S, IMachineExScope<E, S>> {

    infix fun runOnly(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = machineScope.addMappingValue(states) { action(); state }

    infix fun runOnlyS(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = machineScope.scope.launchEx { machineScope.addMappingValue(states) { action(); state } }
}

class StateScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val states: List<S>
) : IStateScope<E, S>, IOnScope<E, S> by onScope {

    override val state: S
        get() = states.first()
}