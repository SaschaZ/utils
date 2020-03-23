package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.ConditionElement.Master
import de.gapps.utils.statemachine.ConditionElement.Slave

interface IMachineExHolder {

    val machine: MachineEx

    suspend fun fire(event: Master.Event, data: Slave? = null) =
        machine.fire(event, data)

    fun fireAndForget(event: Master.Event, data: Slave? = null) =
        machine.fireAndForget(event, data)
}