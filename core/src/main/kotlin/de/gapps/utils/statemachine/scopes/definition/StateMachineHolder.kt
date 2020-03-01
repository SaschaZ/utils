package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope

interface IStateMachineHolder<out EventType : IEvent, out StateType : IState> {
    val stateMachine: IMachineEx<EventType, StateType>
}

class StateMachineHolder<out EventType : IEvent, out StateType : IState>(
    override val stateMachine: IMachineEx<EventType, StateType>
) : IStateMachineHolder<EventType, StateType>


interface IMachineScopeHolder<out EventType : IEvent, out StateType : IState> {
    val machineScope: IMachineExScope<EventType, StateType>
}

data class MachineScopeHolder<out EventType : IEvent, out StateType : IState>(
    override val machineScope: IMachineExScope<EventType, StateType>
) : IMachineScopeHolder<EventType, StateType>