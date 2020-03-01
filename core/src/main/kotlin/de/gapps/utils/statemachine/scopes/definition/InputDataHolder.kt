package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope
import kotlin.reflect.KClass

sealed class InputDataHolder<out E : IEvent, out S : IState>(machine: IStateMachineHolder<E, S>) :
    IStateMachineHolder<E, S> by machine,
    IMachineExScope<E, S> by machine.stateMachine.machineExScope {

    abstract fun matches(
        event: @UnsafeVariance E, state: @UnsafeVariance S,
        previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
    ): Boolean

    infix fun setState(state: @UnsafeVariance S) = addMapping(this, ExecutionHolder { state })

    infix fun executeAndSetState(block: () -> @UnsafeVariance S) = addMapping(this, ExecutionHolder { block() })

    infix fun executeAndSetStateS(block: suspend () -> @UnsafeVariance S) =
        addMapping(this, ExecutionHolder { block() })

    infix fun execute(block: () -> Unit) = addMapping(this, ExecutionHolder { block(); null })

    infix fun executeS(block: suspend () -> Unit) {
        addMapping(this,
            ExecutionHolder { block(); null })
    }

    sealed class EventsWithStatesInput<out E : IEvent, out S : IState>(machine: IStateMachineHolder<E, S>) :
        InputDataHolder<E, S>(machine) {

        interface ITypeWithData<out E : IEvent, out S : IState> : IEventTypeHolder<E>, IStateHolder<S>

        class TypeWithData<out E : IEvent, out S : IState>(
            override val eventTypes: List<KClass<@UnsafeVariance E>>,
            override val states: List<S>,
            machine: IStateMachineHolder<E, S>
        ) : ITypeWithData<E, S>, EventsWithStatesInput<E, S>(machine) {
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
        ) : ITypeWithType<E, S>, EventsWithStatesInput<E, S>(machine) {
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
        ) : IDataWithData<E, S>, EventsWithStatesInput<E, S>(machine) {
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
        ) : IDataWithType<E, S>, EventsWithStatesInput<E, S>(machine) {
            override fun matches(
                event: @UnsafeVariance E, state: @UnsafeVariance S,
                previousChanges: List<Pair<@UnsafeVariance E, @UnsafeVariance S>>
            ): Boolean =
                events.contains(event) && stateTypes.contains(state::class)
        }
    }

    sealed class StatesInput<out S : IState>(machine: IStateMachineHolder<IEvent, S>) :
        InputDataHolder<IEvent, S>(machine) {
        interface IValue<out S : IState> :
            IStateHolder<S>

        class Value<out S : IState>(
            override val states: List<S>,
            machine: IStateMachineHolder<IEvent, S>
        ) : IValue<S>, StatesInput<S>(machine) {
            override fun matches(
                event: IEvent, state: @UnsafeVariance S,
                previousChanges: List<Pair<IEvent, @UnsafeVariance S>>
            ): Boolean = states.contains(state)
        }

        interface IType<out S : IState> :
            IStateTypeHolder<S>

        class Type<out S : IState>(
            override val stateTypes: List<KClass<@UnsafeVariance S>>,
            machine: IStateMachineHolder<IEvent, S>
        ) : IType<S>, StatesInput<S>(machine) {
            override fun matches(
                event: IEvent, state: @UnsafeVariance S,
                previousChanges: List<Pair<IEvent, @UnsafeVariance S>>
            ): Boolean = stateTypes.contains(state::class)
        }
    }
}