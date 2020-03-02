package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx

/**
 * TODO
 */
interface IMachineEx<out D: IData, out E : IEvent, out S : IState> {

    val scope: CoroutineScopeEx

    var event: @UnsafeVariance E?
    val previousEvents: List<E>

    val state: S
    val previousStates: List<S>

    val mapper: IMachineExMapper<D, E, S>

    suspend fun suspendUtilProcessingFinished()

    fun release()
}