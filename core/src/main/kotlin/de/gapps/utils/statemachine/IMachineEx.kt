package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.BaseType.*
import de.gapps.utils.statemachine.BaseType.Primary.*

/**
 * TODO
 */
interface IMachineEx {

    val scope: CoroutineScopeEx

    var event: ValueDataHolder
    val state: ValueDataHolder

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    fun release()
}

