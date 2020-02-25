@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.lvl1.IStateScope
import de.gapps.utils.statemachine.scopes.lvl1.StateChangeScope
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent, out S : IState> {

    val scope: CoroutineScopeEx


    /**********************
     *   EventStateScope  *
     **********************/

    fun addMapping(
        events: List<@UnsafeVariance E>,
        states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    )


    /*****************
     *   StateScope  *
     *****************/

    infix fun IStateScope<@UnsafeVariance E, @UnsafeVariance S>.runOnly(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = addMapping(states, action)

    infix fun IStateScope<@UnsafeVariance E, @UnsafeVariance S>.runOnlyS(
        action: StateChangeScope<@UnsafeVariance S>.() -> Unit
    ) = scope.launchEx { addMapping(states, action) }

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


