package de.gapps.utils.statemachine.scopes.manipulation

import de.gapps.utils.statemachine.IEvent
import de.gapps.utils.statemachine.IState
import de.gapps.utils.statemachine.MachineEx

interface IEventHolder<out E : IEvent> {
    val event: E
}

data class EventHolder<E : IEvent>(override val event: E) :
    IEventHolder<E>

interface ISetScope<out E : IEvent> : IEvent {
    infix fun event(event: @UnsafeVariance E): IEventHolder<IEvent>
    suspend infix fun eventSync(event: @UnsafeVariance E): IEventHolder<IEvent>
}

class SetScope<out E : IEvent, out I : IState>(private val stateMachine: MachineEx<E, I>) :
    ISetScope<E>, MutableMap<String, String> by HashMap() {

    override fun event(event: @UnsafeVariance E): IEventHolder<IEvent> {
        stateMachine.event = event
        return EventHolder(event)
    }

    override suspend fun eventSync(event: @UnsafeVariance E): IEventHolder<IEvent> {
        stateMachine.event = event
        stateMachine.suspendUtilProcessingFinished()
        return EventHolder(event)
    }
}

val <E : IEvent, S : IState> MachineEx<E, S>.set: ISetScope<E>
    get() = SetScope(this)