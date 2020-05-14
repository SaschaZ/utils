package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx
import dev.zieger.utils.statemachine.ConditionElement.Slave
import dev.zieger.utils.statemachine.IConditionElement.IComboElement
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IEvent
import dev.zieger.utils.statemachine.IConditionElement.IMaster.ISingle.IState

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

    val fire: FireScope get() = FireScope(this)

    suspend fun suspendUtilProcessingFinished()

    suspend fun scope(block: suspend MachineDsl.() -> Unit): IMachineEx

    /**
     * Non DSL helper method to fire an [IEvent] with optional [Slave] and suspend until it was processed by the state
     * machine.
     */
    suspend fun fire(combo: IComboElement): IComboElement {
        fire eventSync combo
        return state
    }

    /**
     * Non DSL helper method to add an [IEvent] with optional [Slave] to the [IEvent] processing queue and return
     * immediately.
     */
    fun fireAndForget(combo: IComboElement) = fire event combo

    fun clearPreviousChanges()

    fun release()
}

