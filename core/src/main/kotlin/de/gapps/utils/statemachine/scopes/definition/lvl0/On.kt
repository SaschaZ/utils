package de.gapps.utils.statemachine.scopes.definition.lvl0

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.*
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventScope
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventTypeScope
import kotlin.reflect.KClass

val <E : IEvent, S : IState> IStateMachineHolder<E, S>.on: IOnScope<E, S>
    get() = OnScope(StateMachineHolder(stateMachine))

interface IOnScope<out E : IEvent, out S : IState> : IStateMachineHolder<E, S> {

    infix fun event(events: List<@UnsafeVariance E>) =
        OnScope.EventScope(this, events)

    infix fun state(states: List<@UnsafeVariance S>): SInputDataHolder<IEvent, S> =
        SInputDataHolder.SStatesInput.Value(states, this)

    infix fun eventTypes(eventTypes: List<@UnsafeVariance E>) =
        OnScope.EventTypeScope(
            this,
            eventTypes.map {
                @Suppress("UNCHECKED_CAST")
                it::class as KClass<@kotlin.UnsafeVariance E>
            })
}

class OnScope<out E : IEvent, out S : IState>(stateMachineHolder: StateMachineHolder<E, S>) :
    IOnScope<E, S>, IStateMachineHolder<E, S> by stateMachineHolder {

    class EventScope<out E : IEvent, out S : IState>(
        onScope: IOnScope<E, S>,
        events: List<@UnsafeVariance E>
    ) : IEventScope<E, S>, IOnScope<E, S> by onScope, IEventHolder<E> by EventHolder(events)

    class EventTypeScope<out E : IEvent, out S : IState>(
        onScope: IOnScope<E, S>,
        eventTypes: List<KClass<@UnsafeVariance E>>
    ) : IEventTypeScope<E, S>, IOnScope<E, S> by onScope, IEventTypeHolder<E> by EventTypeHolder(eventTypes)
}