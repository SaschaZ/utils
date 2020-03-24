package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement
import de.gapps.utils.statemachine.IConditionElement.IMaster.IEvent
import de.gapps.utils.statemachine.IConditionElement.ISlave

/**
 * TODO
 */
interface IMachineEx {

    val scope: ICoroutineScopeEx

    var event: ICombinedConditionElement
    val state: ICombinedConditionElement

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    suspend fun fire(event: IEvent, data: ISlave? = null)
    fun fireAndForget(event: IEvent, data: ISlave? = null)

    fun release()
}

