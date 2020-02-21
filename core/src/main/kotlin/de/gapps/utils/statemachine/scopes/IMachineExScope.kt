@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent<*, *>, out S : IState> {

    /**************
     *   OnScope  *
     **************/

    val on: IOnScope<E, S>
        get() = OnScope(this)

    infix fun IOnScope<@UnsafeVariance E, @UnsafeVariance S>.event(event: @UnsafeVariance E) =
        EventScope(this, event)

    infix fun IOnScope<@UnsafeVariance E, @UnsafeVariance S>.events(events: List<@UnsafeVariance E>) =
        EventScope(this, events)


    /*****************
     *   EventScope  *
     *****************/

    infix fun IEventScope<@UnsafeVariance E, @UnsafeVariance S>.withState(state: @UnsafeVariance S) =
        EventStateScope(this, state)

    infix fun IEventScope<@UnsafeVariance E, @UnsafeVariance S>.withStates(states: List<@UnsafeVariance S>) =
        EventStateScope(this, states)


    /**********************
     *   EventStateScope  *
     **********************/

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.run(
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    ) = addMapping(events, states, action)

    infix fun IEventStateScope<@UnsafeVariance E, @UnsafeVariance S>.runOnly(
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

    infix fun IOnScope<@UnsafeVariance E, @UnsafeVariance S>.state(state: @UnsafeVariance S) =
        StateScope(this, state)

    infix fun IOnScope<@UnsafeVariance E, @UnsafeVariance S>.states(states: List<@UnsafeVariance S>) =
        StateScope(this, states)

    infix fun IStateScope<@UnsafeVariance E, @UnsafeVariance S>.runOnly(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = addMapping(states, action)

    fun addMapping(
        states: @UnsafeVariance List<@UnsafeVariance S>,
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    )
}


/***********
 *   Misc  *
 ***********/

operator fun <T : Any> T.div(o: T) = listOf(this, o)

operator fun <T : Any> List<T>.div(o: T) = listOfNotNull(getOrNull(0), getOrNull(1), o)

operator fun <K : Any, V : Any> Pair<K, V>.plus(o: Pair<K, V>) = listOf(this, o)
operator fun <K : Any, V : Any> List<Pair<K, V>>.plus(o: Pair<K, V>): List<Pair<K, V>> =
    toMutableList().apply { add(o) }

operator fun <K : Any, V : Any> K.times(o: V) = this to o


