package de.gapps.utils.statemachine.scopes.definition.lvl0

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.IMachineExScope
import de.gapps.utils.statemachine.scopes.definition.*
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventScope
import de.gapps.utils.statemachine.scopes.definition.lvl1.IEventTypeScope
import de.gapps.utils.statemachine.scopes.definition.lvl1.IStateChangeScope
import de.gapps.utils.statemachine.scopes.definition.lvl1.StateScope
import kotlin.reflect.KClass

val <E : IEvent, S : IState> IMachineExScope<E, S>.on: IOnScope<E, S>
    get() = OnScope(StateMachineHolder(this))

interface IOnScope<out E : IEvent, out S : IState> : IStateMachineHolder<E, S, IMachineExScope<E, S>> {

    infix fun events(events: List<@UnsafeVariance E>) =
        OnScope.EventScope(this, events)

    infix fun states(states: List<@UnsafeVariance S>) =
        StateScope(this, states)

    infix fun eventTypes(eventTypes: List<@UnsafeVariance E>) =
        OnScope.EventTypeScope(
            this,
            eventTypes.map {
                @Suppress("UNCHECKED_CAST")
                it::class as KClass<@kotlin.UnsafeVariance E>
            })
}

class OnScope<out E : IEvent, out S : IState>(stateMachineHolder: IStateMachineHolder<E, S, IMachineExScope<E, S>>) :
    IOnScope<E, S>, IStateMachineHolder<E, S, IMachineExScope<E, S>> by stateMachineHolder {

    class EventScope<out E : IEvent, out S : IState>(
        onScope: IOnScope<E, S>,
        events: List<@UnsafeVariance E>
    ) : IEventScope<E, S>, IOnScope<E, S> by onScope, IEventHolder<E> by EventHolder(events)

    class EventTypeScope<out E : IEvent, out S : IState>(
        onScope: IOnScope<E, S>,
        eventTypes: List<KClass<@UnsafeVariance E>>
    ) : IEventTypeScope<E, S>, IOnScope<E, S> by onScope, IEventTypeHolder<E> by EventTypeHolder(eventTypes)

    class StateChangeScope<out S : IState>(
        onScope: IOnScope<IEvent, S>,
        states: List<S>
    ) : IStateChangeScope<S>, IOnScope<IEvent, S> by onScope, IStateHolder<S> by StateHolder(states)
}