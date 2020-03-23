package de.gapps.utils.statemachine

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.statemachine.ConditionElement.Master.Event
import de.gapps.utils.statemachine.ConditionElement.Slave
import de.gapps.utils.statemachine.IConditionElement.ICombinedConditionElement

/**
 * TODO
 */
interface IMachineEx {

    val scope: CoroutineScopeEx

    var event: ICombinedConditionElement
    val state: ICombinedConditionElement

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    suspend fun fire(event: Event, data: Slave? = null)
    fun fireAndForget(event: Event, data: Slave? = null)

    fun release()
}

