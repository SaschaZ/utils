@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes.definition.lvl1

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.IEventHolder
import de.gapps.utils.statemachine.scopes.definition.IEventTypeHolder
import de.gapps.utils.statemachine.scopes.definition.InputDataHolder
import de.gapps.utils.statemachine.scopes.definition.lvl0.IOnScope
import kotlin.reflect.KClass


interface IEventScope<out E : IEvent, out S : IState> :
    IOnScope<E, S>, IEventHolder<E> {

    infix fun withState(states: List<@UnsafeVariance S>): InputDataHolder<E, S> =
        InputDataHolder.EventsWithStatesInput.DataWithData(events, states, this)
}

class EventScope<out E : IEvent, out S : IState>(
    onScope: IOnScope<E, S>,
    override val events: List<@UnsafeVariance E>
) : IEventScope<E, S>, IOnScope<E, S> by onScope

interface IEventTypeScope<out E : IEvent, out S : IState> :
    IOnScope<E, S>, IEventTypeHolder<E> {
    override val eventTypes: List<KClass<@UnsafeVariance E>>
    val event
        get() = eventTypes.first()

    infix fun withStates(states: List<@UnsafeVariance S>): InputDataHolder<E, S> =
        InputDataHolder.EventsWithStatesInput.TypeWithData(eventTypes, states, this)
}