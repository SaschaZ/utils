package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope
import kotlin.reflect.KClass


interface IStateMachineHolder<out EventType : IEvent, out StateType : IState, out MachineScope : IMachineExScope<EventType, StateType>> {
    val machineScope: MachineScope
}

data class StateMachineHolder<out EventType : IEvent, out StateType : IState, out MachineScope : IMachineExScope<EventType, StateType>>(
    override val machineScope: MachineScope
) : IStateMachineHolder<EventType, StateType, MachineScope>


interface IEventHolder<out E : IEvent> {
    val events: List<E>
    val event
        get() = events.first()
}

data class EventHolder<out E : IEvent>(override val events: List<E>) : IEventHolder<E>


interface IEventTypeHolder<out E : IEvent> {
    val eventTypes: List<KClass<@UnsafeVariance E>>
    val eventType
        get() = eventTypes.first()
}

data class EventTypeHolder<out E : IEvent>(override val eventTypes: List<KClass<@UnsafeVariance E>>) :
    IEventTypeHolder<E>


interface IStateHolder<out S : IState> {
    val states: List<S>
    val state
        get() = states.first()
}

data class StateHolder<out S : IState>(override val states: List<S>) : IStateHolder<S>


interface IStateTypeHolder<out S : IState> {
    val stateTypes: List<KClass<@UnsafeVariance S>>
    val stateType: KClass<@UnsafeVariance S>
        get() = stateTypes.first()
}

data class StateTypeHolder<out S : IState>(override val stateTypes: List<KClass<@UnsafeVariance S>>) :
    IStateTypeHolder<S>

sealed class SInputDataHolder {
    sealed class SEventsWithStatesInput : SInputDataHolder() {
        interface ITypeWithData<out E : IEvent, out S : IState> : IEventTypeHolder<E>, IStateHolder<S>
        class TypeWithData<out E : IEvent, out S : IState>(
            override val eventTypes: List<KClass<@UnsafeVariance E>>,
            override val states: List<S>
        ) : ITypeWithData<E, S>, SEventsWithStatesInput()

        interface ITypeWithType<out E : IEvent, out S : IState> : IEventTypeHolder<E>, IStateTypeHolder<S>
        class TypeWithType<out E : IEvent, out S : IState>(
            override val eventTypes: List<KClass<@UnsafeVariance E>>,
            override val stateTypes: List<KClass<@UnsafeVariance S>>
        ) : ITypeWithType<E, S>, SEventsWithStatesInput()

        interface IDataWithData<out E : IEvent, out S : IState> : IEventHolder<E>, IStateHolder<S>
        class DataWithData<out E : IEvent, out S : IState>(
            override val events: List<E>,
            override val states: List<S>
        ) : IDataWithData<E, S>, SEventsWithStatesInput()

        interface IDataWithType<out E : IEvent, out S : IState> : IEventHolder<E>, IStateTypeHolder<S>
        class DataWithType<out E : IEvent, out S : IState>(
            override val events: List<E>,
            override val stateTypes: List<KClass<@UnsafeVariance S>>
        ) : IDataWithType<E, S>, SEventsWithStatesInput()
    }

    sealed class SStatesInput : SInputDataHolder() {
        interface IValue<out S : IState> : IStateHolder<S>
        class Value<out S : IState>(override val states: List<S>) : IValue<S>, SStatesInput()

        interface IType<out S : IState> : IStateTypeHolder<S>
        class Type<out S : IState>(override val stateTypes: List<KClass<@UnsafeVariance S>>) : IType<S>, SStatesInput()
    }
}

interface IExecutionHolder<out RS : IState> {
    fun execute(): IResultingStateHolder<RS>
}

class ExecutionHolder<out RS : IState>(val toExecute: () -> RS) : IExecutionHolder<RS> {
    override fun execute() = ResultingStateHolder(toExecute())
}

interface IResultingStateHolder<out S : IState> {
    val resultingState: S
}

data class ResultingStateHolder<out S : IState>(override val resultingState: S) : IResultingStateHolder<S>
