package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent, out S : IState> {

    /**************
     *   OnScope  *
     **************/

    val on: IOnScope
        get() = OnScope()

    infix fun IOnScope.event(event: @UnsafeVariance E) = EventScope(this, event)
    infix fun IOnScope.events(events: List<@UnsafeVariance E>) = EventScope(this, events)


    /*****************
     *   EventScope  *
     *****************/

    infix fun IEventScope<@UnsafeVariance E>.withState(state: @UnsafeVariance S) = EventStateScope(this, state)
    infix fun IEventScope<@UnsafeVariance E>.withStates(states: List<@UnsafeVariance S>) = EventStateScope(this, states)


    /**********************
     *   EventStateScope  *
     **********************/

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.run(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = addMapping(events, states, action)

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.onlyRun(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> Unit
    ) = addMapping(events, states) { action(); states.first() }

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.changeTo(toState: @UnsafeVariance S) =
        addMapping(events, states) { toState }

    fun addMapping(
        events: List<@UnsafeVariance E>,
        states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    )


    /*****************
     *   StateScope  *
     *****************/

    infix fun IOnScope.state(state: @UnsafeVariance S) = StateScope(this, state)
    infix fun IOnScope.states(states: List<@UnsafeVariance S>) = StateScope(this, states)

    infix fun IStateScope<@UnsafeVariance S>.onlyRun(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = addMapping(states, action)

    fun addMapping(
        states: @UnsafeVariance List<@UnsafeVariance S>,
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    )


    /***********
     *   Misc  *
     ***********/

    infix fun @UnsafeVariance E.or(o: @UnsafeVariance E) = listOf(this, o)
    infix fun List<@UnsafeVariance E>.or(o: @UnsafeVariance E): List<E> = toMutableList().also { it.add(o) }

    infix fun @UnsafeVariance S.or(o: @UnsafeVariance S) = listOf(this, o)
    infix fun List<@UnsafeVariance S>.or(o: @UnsafeVariance S): List<S> = toMutableList().also { it.add(o) }
}


