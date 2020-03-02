package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx

/**
 * TODO
 */
interface IMachineEx {

    val scope: CoroutineScopeEx

    var event: @UnsafeVariance IEvent?
    val previousEvents: List<IEvent>

    val state: IState
    val previousStates: List<IState>

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    fun release()
}