@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.lvl1.StateChangeScope
import de.gapps.utils.statemachine.scopes.lvl4.EventChangeScope
import kotlin.reflect.KClass

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent, out S : IState> {

    val scope: CoroutineScopeEx


    /**********************
     *   EventStateScope  *
     **********************/

    fun addMappingValue(
        events: List<@UnsafeVariance E>,
        states: List<@UnsafeVariance S>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    )

    fun addMappingType(
        eventTypes: List<KClass<@UnsafeVariance E>>,
        stateTypes: List<KClass<@UnsafeVariance S>>,
        action: EventChangeScope<@UnsafeVariance E, @UnsafeVariance S>.() -> @UnsafeVariance S
    )

    fun addMappingValue(
        states: @UnsafeVariance List<@UnsafeVariance S>,
        action: StateChangeScope<@UnsafeVariance S>.() -> @UnsafeVariance S
    )
}


/***********
 *   Misc  *
 ***********/

operator fun <T : Any> T.div(o: T) = listOf(this, o)

operator fun <T : Any> List<T>.div(o: T) = listOfNotNull(getOrNull(0), getOrNull(1), o)

operator fun <K : Any, V : Any> K.times(o: V) = this to o

operator fun <T> T.unaryPlus(): List<T> = listOf(this)

operator fun <T> List<T>.plus(o: T) = toMutableList().apply { add(o) }


