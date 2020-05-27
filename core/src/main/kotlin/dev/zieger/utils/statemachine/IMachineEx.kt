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

    suspend fun suspendUtilProcessingFinished()

    suspend fun fire(combo: IComboElement): IComboElement {
        fireAndForget(combo)
        suspendUtilProcessingFinished()
        return state
    }

    fun fireAndForget(combo: IComboElement) {
        event = combo
    }

    fun clearPreviousChanges()

    fun release()
}

