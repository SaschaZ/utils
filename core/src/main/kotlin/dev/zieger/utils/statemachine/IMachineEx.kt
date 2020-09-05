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

    val eventCombo: ComboEventElement
    val event: Event? get() = eventCombo.master
    val eventData: Data? get() = eventCombo.slave as? Data

    suspend fun setEvent(event: ComboEventElement): ComboStateElement
    suspend fun setEvent(event: Event, data: Data? = null): ComboStateElement=
        setEvent(event.comboEvent(data))

    val stateCombo: ComboStateElement
    val state: State? get() = stateCombo.master
    val stateData: Data? get() = stateCombo.slave as? Data

    suspend fun suspendUtilProcessingFinished()

    fun fireAndForget(combo: ComboEventElement) = scope.launchEx { setEvent(combo) }.asUnit()

    fun clearPreviousChanges()

    fun release()
}

