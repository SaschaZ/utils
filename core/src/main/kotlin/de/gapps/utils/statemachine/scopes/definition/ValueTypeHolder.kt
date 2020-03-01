package de.gapps.utils.statemachine.scopes.definition

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import kotlin.reflect.KClass



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


