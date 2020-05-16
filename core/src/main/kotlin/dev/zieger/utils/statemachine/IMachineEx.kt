package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx
import dev.zieger.utils.statemachine.conditionelements.IComboElement
import dev.zieger.utils.statemachine.conditionelements.IEvent
import dev.zieger.utils.statemachine.conditionelements.IState

/**
 * TODO
 */
interface IMachineEx {

    val scope: ICoroutineScopeEx

    var event: IComboElement
    fun event() = event.master as? IEvent
    val state: IComboElement
    fun state() = state.master as? IState

    val mapper: IMachineExMapper

    suspend fun suspendUtilProcessingFinished()

    suspend fun fire(combo: IComboElement): IComboElement
    fun fireAndForget(combo: IComboElement)

    fun clearPreviousChanges()

    fun release()
}

