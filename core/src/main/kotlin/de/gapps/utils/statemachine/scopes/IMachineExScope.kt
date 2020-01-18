package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent, out S : IState> {

    val on: IOnScope<E>
        get() = OnScope()

    infix fun IOnScope<@UnsafeVariance E>.event(event: @UnsafeVariance E) = EventScope(event)
    infix fun IOnScope<@UnsafeVariance E>.eventOf(events: List<@UnsafeVariance E>) = EventScope(events)
    infix fun IOnScope<@UnsafeVariance E>.state(state: @UnsafeVariance S) = StateScope(state)


    infix fun IEventScope<@UnsafeVariance E>.with(state: @UnsafeVariance S) = EventStateScope(events, state)
    infix fun IEventScope<@UnsafeVariance E>.withOneOf(states: List<@UnsafeVariance S>) =
        EventStateScope(events, states)


    fun addMapping(
        events: List<@UnsafeVariance E>,
        states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    )

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.run(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) =
        addMapping(events, states, action)

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.changeTo(
        toState: @UnsafeVariance S
    ) =
        addMapping(events, states) { toState }

    infix fun IStateScope<@UnsafeVariance S>.run(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) =
        addMapping(states, action)

    fun addMapping(
        states: @UnsafeVariance List<@UnsafeVariance S>,
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    )
}


