package de.gapps.utils.statemachine.scopes.lvl0

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.MachineEx


interface ISetScope<out E : IEvent> : IEvent {
    infix fun event(event: @UnsafeVariance E): ISetScope<IEvent>
    suspend infix fun eventSync(event: @UnsafeVariance E): ISetScope<IEvent>
}

class SetScope<out E : IEvent, out I : IState>(private val stateMachine: MachineEx<E, I>) :
    ISetScope<E>, MutableMap<String, String> by HashMap() {

    override fun event(event: @UnsafeVariance E): ISetScope<IEvent> {
        stateMachine.event = event
        return this
    }

    override suspend fun eventSync(event: @UnsafeVariance E): ISetScope<IEvent> {
        stateMachine.event = event
        stateMachine.suspendUtilProcessingFinished()
        return this
    }
}

val <E : IEvent, S : IState> MachineEx<E, S>.set: ISetScope<E>
    get() = SetScope(this)