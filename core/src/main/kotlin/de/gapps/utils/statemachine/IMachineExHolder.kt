package de.gapps.utils.statemachine

interface IMachineExHolder {

    val machine: MachineEx

    suspend fun fire(event: BaseType.Primary.Event, data: BaseType.Data? = null) =
        machine.fire(event, data)

    fun fireAndForget(event: BaseType.Primary.Event, data: BaseType.Data? = null) =
        machine.fireAndForget(event, data)
}