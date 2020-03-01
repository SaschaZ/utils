@file:Suppress("unused")

package de.gapps.utils.statemachine.scopes

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.scopes.definition.IExecutionHolder
import de.gapps.utils.statemachine.scopes.definition.InputDataHolder

/**
 * Scope that provides easy dsl to define state machine behaviour.
 */
interface IMachineExScope<out E : IEvent, out S : IState> {

    val scope: CoroutineScopeEx

    fun addMapping(
        data: InputDataHolder<@UnsafeVariance E, @UnsafeVariance S>,
        executionHolder: IExecutionHolder<@UnsafeVariance S>
    ) {
        dataToExecutionMap[data] = executionHolder
    }

    val dataToExecutionMap: HashMap<InputDataHolder<@UnsafeVariance E, @UnsafeVariance S>, IExecutionHolder<@UnsafeVariance S>>
}


/***********
 *   Misc  *
 ***********/

operator fun <T : Any> T.div(o: T) = listOfNotNull(this, o)

operator fun <T : Any> List<T>.div(o: T) = listOfNotNull(getOrNull(0), getOrNull(1), o)

operator fun <K : Any, V : Any> K.times(o: V) = this to o

operator fun <T : Any> T?.unaryPlus(): List<T> = listOfNotNull(this)

operator fun <T> List<T>.plus(o: T): List<T> = toMutableList().apply { add(o) }


