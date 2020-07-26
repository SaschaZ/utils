package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.statemachine.conditionelements.*

/**
 * TODO
 */
interface IMachineEx {

    val scope: ICoroutineScopeEx

    val eventCombo: IComboElement
    val event: IEvent? get() = eventCombo.event
    val eventData: IData? get() = eventCombo.slave as? IData

    suspend fun setEvent(event: IComboElement): IComboElement
    suspend fun setEvent(event: IEvent, data: IData? = null): IComboElement =
        setEvent(event.combo.also { it.slave = data })

    val stateCombo: IComboElement
    val state: IState? get() = stateCombo.state
    val stateData: IData? get() = stateCombo.slave as? IData

    suspend fun suspendUtilProcessingFinished()

    fun fireAndForget(combo: IComboElement) = scope.launchEx { setEvent(combo) }.asUnit()

    fun clearPreviousChanges()

    fun release()
}

