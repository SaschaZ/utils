package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle.IState
import de.gapps.utils.statemachine.IConditionElement.ISlave

/**
 * TODO
 */
interface IMachineEx {

    val scope: ICoroutineScopeEx

    var event: IEvent
    val state: IState

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    suspend fun fire(event: IEvent, data: ISlave? = null)
    fun fireAndForget(event: IEvent, data: ISlave? = null)

    fun release()
}

