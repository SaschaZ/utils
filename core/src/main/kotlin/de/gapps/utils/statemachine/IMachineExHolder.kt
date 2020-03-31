package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.IComboElement

interface IMachineExHolder {

    val machine: IMachineEx

    suspend fun fire(combo: IComboElement) =
        machine.fire(combo)

    fun fireAndForget(combo: IComboElement) =
        machine.fireAndForget(combo)
}