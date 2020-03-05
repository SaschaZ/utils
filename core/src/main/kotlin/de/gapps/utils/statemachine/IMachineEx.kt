package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx

/**
 * TODO
 */
interface IMachineEx<out D : Any> {

    val scope: CoroutineScopeEx

    var event: @UnsafeVariance IEvent<@UnsafeVariance D>?
    val previousEvents: List<IEvent<@UnsafeVariance D>>

    val state: IState<@UnsafeVariance D>
    val previousStates: List<IState<@UnsafeVariance D>>

    val mapper: IMachineExMapper<D>

    suspend fun suspendUtilProcessingFinished()

    fun release()
}