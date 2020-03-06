package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx

/**
 * TODO
 */
interface IMachineEx {

    val scope: CoroutineScopeEx

    var event: ValueDataHolder<Event>?
    val previousEvents: List<ValueDataHolder<Event>>

    val state: ValueDataHolder<State>
    val previousStates: List<ValueDataHolder<State>>

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    fun release()
}

