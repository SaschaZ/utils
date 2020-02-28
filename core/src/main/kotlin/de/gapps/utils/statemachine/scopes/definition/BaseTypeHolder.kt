package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IMachineEx
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope
import kotlin.reflect.KClass

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

sealed class SInputDataHolder<out E : IEvent, out S : IState>(machine: IStateMachineHolder<E, S>) :
    IStateMachineHolder<E, S> by machine, IMachineExScope<E, S> by machine.stateMachine.machineExScope {

    abstract fun matches(
        event: @UnsafeVariance E, state: @UnsafeVariance S,
        previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
    ): Boolean

    infix fun setState(state: @UnsafeVariance S) = addMapping(this, ExecutionHolder { null })

    infix fun executeAndSetState(block: () -> @UnsafeVariance S) = addMapping(this, ExecutionHolder { block() })

    infix fun executeAndSetStateS(block: suspend () -> @UnsafeVariance S) =
        addMapping(this, ExecutionHolder { block() })

    infix fun execute(block: () -> Unit) {
        addMapping(this, ExecutionHolder { block(); null })
    }

    infix fun executeS(block: suspend () -> Unit) {
        addMapping(this, ExecutionHolder { block(); null })
    }

    sealed class SEventsWithStatesInput<out E : IEvent, out S : IState>(machine: IStateMachineHolder<E, S>) :
        SInputDataHolder<E, S>(machine) {

        interface ITypeWithData<out E : IEvent, out S : IState> : IEventTypeHolder<E>, IStateHolder<S>
        class TypeWithData<out E : IEvent, out S : IState>(
            override val eventTypes: List<KClass<@UnsafeVariance E>>,
            override val states: List<S>,
            machine: IStateMachineHolder<E, S>
        ) : ITypeWithData<E, S>, SEventsWithStatesInput<E, S>(machine) {
            override fun matches(
                event: @UnsafeVariance E, state: @UnsafeVariance S,
                previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
            ): Boolean =
                eventTypes.contains(event::class) && states.contains(state)
        }

        interface ITypeWithType<out E : IEvent, out S : IState> : IEventTypeHolder<E>, IStateTypeHolder<S>
        class TypeWithType<out E : IEvent, out S : IState>(
            override val eventTypes: List<KClass<@UnsafeVariance E>>,
            override val stateTypes: List<KClass<@UnsafeVariance S>>,
            machine: IStateMachineHolder<E, S>
        ) : ITypeWithType<E, S>, SEventsWithStatesInput<E, S>(machine) {
            override fun matches(
                event: @UnsafeVariance E, state: @UnsafeVariance S,
                previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
            ): Boolean =
                eventTypes.contains(event::class) && stateTypes.contains(state::class)
        }

        interface IDataWithData<out E : IEvent, out S : IState> : IEventHolder<E>, IStateHolder<S>
        class DataWithData<out E : IEvent, out S : IState>(
            override val events: List<E>,
            override val states: List<S>,
            machine: IStateMachineHolder<E, S>
        ) : IDataWithData<E, S>, SEventsWithStatesInput<E, S>(machine) {
            override fun matches(
                event: @UnsafeVariance E, state: @UnsafeVariance S,
                previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
            ): Boolean =
                events.contains(event) && states.contains(state)
        }

        interface IDataWithType<out E : IEvent, out S : IState> : IEventHolder<E>, IStateTypeHolder<S>
        class DataWithType<out E : IEvent, out S : IState>(
            override val events: List<E>,
            override val stateTypes: List<KClass<@UnsafeVariance S>>,
            machine: IStateMachineHolder<E, S>
        ) : IDataWithType<E, S>, SEventsWithStatesInput<E, S>(machine) {
            override fun matches(
                event: @UnsafeVariance E, state: @UnsafeVariance S,
                previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
            ): Boolean =
                events.contains(event) && stateTypes.contains(state::class)
        }
    }

    sealed class SStatesInput<out S : IState>(machine: IStateMachineHolder<IEvent, S>) :
        SInputDataHolder<IEvent, S>(machine) {
        interface IValue<out S : IState> : IStateHolder<S>
        class Value<out S : IState>(
            override val states: List<S>,
            machine: IStateMachineHolder<IEvent, S>
        ) : IValue<S>, SStatesInput<S>(machine) {
            override fun matches(
                event: IEvent, state: @UnsafeVariance S,
                previousChanges: List<Pair<IEvent, @UnsafeVariance S>>
            ): Boolean = states.contains(state)
        }

        interface IType<out S : IState> : IStateTypeHolder<S>
        class Type<out S : IState>(
            override val stateTypes: List<KClass<@UnsafeVariance S>>,
            machine: IStateMachineHolder<IEvent, S>
        ) : IType<S>, SStatesInput<S>(machine) {
            override fun matches(
                event: IEvent, state: @UnsafeVariance S,
                previousChanges: List<Pair<IEvent, @UnsafeVariance S>>
            ): Boolean = stateTypes.contains(state::class)
        }
    }
}

interface IExecutionHolder<out RS : IState> {
    suspend fun execute(): IResultingStateHolder<RS>?
}

class ExecutionHolder<out RS : IState>(val toExecute: suspend () -> RS?) : IExecutionHolder<RS> {
    override suspend fun execute() = toExecute()?.let { ResultingStateHolder(it) }
}

interface IResultingStateHolder<out S : IState> {
    val resultingState: S
}

data class ResultingStateHolder<out S : IState>(override val resultingState: S) : IResultingStateHolder<S>