package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.IComboElement


interface IMachineExHolder {

    val machine: IMachineEx

    suspend fun fire(combo: IComboElement) =
        machine.fire(combo)

    fun fireAndForget(combo: IComboElement) =
        machine.fireAndForget(combo)
}