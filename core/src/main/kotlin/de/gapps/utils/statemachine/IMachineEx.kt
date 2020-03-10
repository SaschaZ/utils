package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.BaseType.ValueDataHolder

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

