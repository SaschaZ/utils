package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.IConditionElement.IMaster.ISingle
import de.gapps.utils.statemachine.IConditionElement.ISlave

interface IMachineExHolder {

    val machine: MachineEx

    suspend fun fire(event: ISingle.IEvent, data: ISlave? = null) =
        machine.fire(event, data)

    fun fireAndForget(event: ISingle.IEvent, data: ISlave? = null) =
        machine.fireAndForget(event, data)
}