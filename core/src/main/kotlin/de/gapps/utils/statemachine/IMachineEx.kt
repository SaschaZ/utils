package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx

/**
 * TODO
 */
interface IMachineEx {

    val scope: CoroutineScopeEx

    var event: ValueDataHolder?
    val previousEvents: List<ValueDataHolder>

    val state: ValueDataHolder
    val previousStates: List<ValueDataHolder>

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    fun release()
}

